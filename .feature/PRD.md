# 대규모 Android 프로젝트 내 React Native 브라운필드 통합을 위한 초기 환경 구축

## 개요 (Project Overview)

본 프로젝트는 기존의 대규모 Android 네이티브 앱(Jetpack Compose 기반)에 React Native 모듈을 점진적으로 통합하기 위한 최적의 기술 환경을 구축하는 것을 목표로 한다. 시니어 엔지니어 관점에서 유지보수성과 확장성을 고려하여 Nx 모노레포 아키텍처를 기반으로 설계한다.

## 목표 (Goals)

Nx를 활용한 Android와 RN 프로젝트의 단일 저장소(Monorepo) 관리.
Jetpack Compose 환경에서 Fragment 없이 RN 화면을 임베딩하는 기술적 토대 마련.
네이티브(Hilt)와 RN 간의 의존성 충돌 없는 빌드 파이프라인 구성.

## 저장소 구조 (Directory Structure)

{root}/
├── apps/
│ ├── AndroidApp/ # 기존/신규 네이티브 Android 프로젝트 (Jetpack Compose)
│ └── RnApp/ # 신규 React Native 프로젝트 (JS/TS logic)
├── node_modules/ # 루트 레벨의 의존성 관리
├── package.json # 모노레포 전체 의존성 및 Nx 설정
├── nx.json # Nx 빌드 시스템 설정
└── workspace.json # 프로젝트 정의

## 기술 스택 요구사항 (Technical Stack)

### 1. 모노레포 및 빌드 도구

- Tool: Nx (Latest)
- Task Runner: nx를 통해 Android 빌드 및 RN 메트로 서버 실행 제어.

### 2. Android 프로젝트 (apps/AndroidApp)

- Min SDK: 29
- UI Framework: Jetpack Compose (Full implementation)
- Dependency Injection: Hilt
- Navigation: Jetpack Compose Navigation
- Architecture: Clean Architecture 기반 멀티모듈 구조 (초기 단계에서는 단일 모듈 내 패키지 분리로 시작)
- UI 요구사항:
  - 하단 바텀 네비게이션바 (5개 탭: 홈, 내역, 검색, 채팅, 설정)
  - 각 화면 중앙에 해당 화면의 이름을 알리는 Text 배치.

### 3. React Native 프로젝트 (apps/RnApp)

- Type: Bare React Native (Not Expo, 프로젝트 커스텀을 위해)
- Version: 최신 안정화 버전 (0.73+ 권장)
- Template: TypeScript

## 상세 구현 가이드 (Detailed Requirements)

### 1. Android 네비게이션 및 UI 구성

- Scaffold를 사용하여 BottomNavigation을 구현한다.
- 각 탭 이동 시 Compose Navigation(NavHost)을 사용한다.
- RN으로 전환할 '어드민 앱'의 기능이 들어갈 자리를 위해 특정 탭(예: '설정')에 RN 뷰를 호출할 준비를 한다.

### 2. 브라운필드(Brownfield) 통합 설계

- Fragment 미사용 원칙: RN의 ReactRootView를 Compose의 AndroidView 컴포저블로 래핑하여 네이티브 Compose 화면 내에 직접 삽입한다.
- RN 런타임 관리: 앱 전체에서 ReactNativeHost를 싱글톤으로 관리할 수 있도록 Hilt를 통해 주입하거나 Application 클래스에서 초기화한다.

### 3. 빌드 설정 (Gradle & Nx)

- Settings.gradle: 루트 디렉토리의 node_modules 내에 있는 @react-native/gradle-plugin을 참조하도록 설정한다.
- App build.gradle: com.facebook.react 플러그인을 적용하고, react-android 및 hermes-android 의존성을 추가한다.
- Nx Integration: apps/AndroidApp 프로젝트에 project.json을 생성하여 nx build-android와 같은 명령어로 빌드가 가능하도록 타겟을 정의한다.

## 학습 포인트 (Learning Milestones for User)

- 의존성 연결: 루트의 package.json에 정의된 RN 라이브러리가 어떻게 네이티브 Android Gradle 빌드 시스템에 연결되는지 이해.
- AndroidView 래핑: Compose 환경에서 네이티브 View인 ReactRootView를 어떻게 생명주기에 맞춰 렌더링하는지 학습.
- Metro 서버 통신: 네이티브 앱 실행 시 RN 번들을 Metro 서버로부터 로드하는 과정 확인.

## 수락 기준 (Acceptance Criteria)

- nx run-android AndroidApp 실행 시 에뮬레이터에서 5개 탭을 가진 앱이 구동되어야 함.
- Android 프로젝트에서 Hilt 에러 없이 컴파일이 완료되어야 함.
- apps/RnApp 프로젝트의 index.js 수정 시 메트로 서버를 통해 Android 앱 내 RN 영역(추후 구현 예정)에 반영될 준비가 완료되어야 함.
