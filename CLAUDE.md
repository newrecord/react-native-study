# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

대규모 Android 네이티브 앱(Jetpack Compose)에 React Native를 브라운필드(Brownfield) 방식으로 통합하기 위한 학습/실험용 모노레포. Nx로 Android와 RN 프로젝트를 단일 저장소에서 관리한다.

## 빌드 및 실행 명령어

모든 명령어는 프로젝트 루트에서 실행한다.

```bash
# Android 앱 빌드 (에뮬레이터/디바이스 필요)
npx nx build-android AndroidApp
npx nx run-android AndroidApp
npx nx clean AndroidApp

# React Native Metro 서버 시작
npx nx start RnApp

# RN 앱 실행
npx nx run-android RnApp
npx nx run-ios RnApp

# 루트 package.json 스크립트
npm run start:rn        # Metro 서버
npm run build:android   # Android 빌드
npm run run:android     # Android 실행

# 프로젝트 관리
npx nx show projects    # 프로젝트 목록
npx nx graph            # 의존성 그래프
```

## 테스트 및 린트

```bash
# RN 앱 테스트 (Jest, apps/RnApp 디렉토리에서)
cd apps/RnApp && npm test

# RN 앱 린트 (ESLint, apps/RnApp 디렉토리에서)
cd apps/RnApp && npm run lint

# Android 테스트 (apps/AndroidApp 디렉토리에서)
cd apps/AndroidApp && ./gradlew test
```

## 아키텍처

### 모노레포 구조

- **Nx + NPM workspaces** 기반 모노레포. `apps/*`가 워크스페이스 패키지.
- `apps/AndroidApp/` — Jetpack Compose + Hilt 네이티브 Android 앱
- `apps/RnApp/` — Bare React Native (TypeScript, Expo 미사용)
- 루트 `node_modules/`에서 공유 의존성 관리. Metro의 `watchFolders`로 루트 node_modules를 참조하도록 설정됨.

### Android 앱 (apps/AndroidApp)

- **UI**: Jetpack Compose (XML 없음) + Material3 + Compose Navigation
- **DI**: Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`)
- **빌드**: Gradle Kotlin DSL + Version Catalog (`gradle/libs.versions.toml`)로 버전 중앙관리
- **SDK**: compileSdk 35, minSdk 29, targetSdk 35, Java/Kotlin 17
- **구조**:
  - `MainActivity` → `AppNavigation()` (NavHost 기반 라우팅)
  - `BottomNavBar` — 5개 탭 (홈, 내역, 검색, 채팅, 설정)
  - `di/AppModule` — Hilt 싱글톤 모듈 (ReactNativeHost 주입 준비)
  - 각 화면(`screens/`)은 탭 이름을 중앙에 표시하는 단순 Compose 컴포저블

### React Native 앱 (apps/RnApp)

- **Bare RN 0.83.1** + React 19 + TypeScript 5.8
- **진입점**: `index.js` → `App.tsx` (SafeAreaProvider 사용)
- **린트/포맷**: ESLint(`@react-native` 확장), Prettier(싱글쿼트, trailing comma)
- **테스트**: Jest 29 + react-test-renderer

### 브라운필드 통합 설계 (핵심)

이 프로젝트의 궁극적 목표는 Compose 화면 안에 RN 뷰를 삽입하는 것이다:
- **Fragment 미사용** — `ReactRootView`를 Compose의 `AndroidView`로 래핑
- **ReactNativeHost**는 Hilt를 통해 싱글톤으로 관리
- 설정 탭 등 특정 화면에서 RN 뷰를 호출할 예정
- Gradle에서 `@react-native/gradle-plugin` 참조 및 `react-android`, `hermes-android` 의존성 추가 필요

## 주요 설정 파일

| 파일 | 역할 |
|------|------|
| `nx.json` | Nx 태스크 캐싱, base 브랜치(`main`) 설정 |
| `apps/AndroidApp/project.json` | Nx Android 빌드 타겟 정의 |
| `apps/RnApp/project.json` | Nx RN 빌드 타겟 정의 |
| `apps/RnApp/metro.config.js` | 모노레포용 watchFolders 설정 |
| `apps/AndroidApp/gradle/libs.versions.toml` | Android 의존성 버전 카탈로그 |

## 코드 스타일 규칙

- **RN**: 싱글쿼트, arrow parens 생략, trailing comma 전체 적용 (`.prettierrc.js` 참고)
- **Android**: Kotlin official 코드 스타일 (`gradle.properties`의 `kotlin.code.style=official`)
- 문서 및 UI 라벨은 **한국어** 사용
