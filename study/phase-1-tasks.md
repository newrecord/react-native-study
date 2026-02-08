# Phase 1: Android 브라운필드 RN 통합 학습 태스크

> 대규모 Android 네이티브 앱(Jetpack Compose + Hilt)에 React Native를 브라운필드 방식으로 통합하기 위한 단계별 학습 가이드.
> 각 Task는 순서대로 진행하며, 이전 Task가 완료되어야 다음으로 넘어갈 수 있다.

---

## Task 1: 빌드 환경 버전 통일 [완료]

### 목표
AndroidApp의 빌드 도구 버전을 RN 통합이 가능한 수준으로 업데이트한다.

### 엔터프라이즈 제약사항
- **Gradle 8.9 변경 불가** (유관부서 승인 필요). 이로 인해:
  - AGP 최대 8.7.x까지만 사용 가능 (AGP 8.8+는 Gradle 8.10.2+ 요구)
  - RN 0.83.1의 `@react-native/gradle-plugin`은 AGP 8.12.0을 요구하므로 **직접 사용 불가**
  - → Task 2에서 RN Gradle Plugin 없이 수동으로 의존성을 추가하는 방식으로 진행

### 실제 적용된 변경

| 항목 | 변경 전 | 변경 후 | 비고 |
|------|--------|--------|------|
| Gradle Wrapper | 8.9 | **8.9 (유지)** | 엔터프라이즈 제약 |
| AGP | 8.7.3 | **8.7.3 (유지)** | Gradle 8.9 최대 호환 |
| Kotlin | 2.0.21 | **2.1.20** | RnApp과 통일 |
| KSP | 2.0.21-1.0.28 | **2.1.20-1.0.31** | Kotlin 버전 매칭 |
| compileSdk | 35 | **36** | RnApp과 통일 |
| targetSdk | 35 | **36** | RnApp과 통일 |
| NDK | 미설정 | **27.1.12297006** | Hermes/JSI 빌드용 |
| Node.js | >=18 | **>=20** | RnApp 요구사항 |
| gradle.properties | - | **newArchEnabled, hermesEnabled 추가** | RN 통합 준비 |

### 학습 포인트
- **AGP-Gradle 호환성 매트릭스**: 엔터프라이즈에서 Gradle 버전이 고정되면 사용 가능한 AGP 범위가 결정되고, 이것이 RN Gradle Plugin 사용 가능 여부를 좌우한다.
- **RN Gradle Plugin 우회 전략**: Plugin을 사용할 수 없을 때 `react-android`, `hermes-android`를 Maven 의존성으로 직접 추가하고, JS 번들링을 수동으로 처리하는 방식이 대안이다.
- Kotlin/KSP 버전은 Gradle 버전과 독립적으로 업데이트 가능하다.

### 완료 기준 [달성]
- [x] AndroidApp 빌드 성공 (`./gradlew assembleDebug`)
- [x] 디바이스에서 앱 실행 및 정상 동작 확인
- [x] RnApp 빌드에 영향 없음

---

## Task 2: AndroidApp Gradle에 React Native 의존성 추가

### 목표
AndroidApp의 Gradle 빌드 설정에 React Native 런타임 의존성(`react-android`, `hermes-android`)을 추가하여, 네이티브 앱에서 RN 런타임을 로드할 수 있는 빌드 환경을 구성한다.

### 배경
**엔터프라이즈 제약**: Gradle 8.9 고정 → AGP 8.7.3 → RN 0.83.1의 `@react-native/gradle-plugin`(AGP 8.12.0 요구) 사용 불가.
따라서 **RN Gradle Plugin 없이 수동으로 의존성을 추가하는 방식**으로 진행한다. 이는 실제 대형 프로젝트에서도 빌드 시스템 변경이 어려울 때 자주 사용되는 현실적인 접근법이다.

### 작업 항목

- [ ] **settings.gradle.kts 수정**
  - `dependencyResolutionManagement`에 RN Maven 저장소 추가 (Maven Central에 배포된 `react-android`, `hermes-android` 사용)
  - `FAIL_ON_PROJECT_REPOS` 정책과의 충돌 해결 (필요 시 `PREFER_SETTINGS`로 변경)
- [ ] **app/build.gradle.kts 수정**
  - `react-android`, `hermes-android` 의존성 직접 추가 (버전 명시)
  - RN Gradle Plugin 없이 수동 설정
- [ ] **libs.versions.toml 업데이트**
  - RN 관련 라이브러리 버전 카탈로그에 등록 (reactNative = "0.83.1")
- [ ] 의존성 트리 확인으로 충돌 검증: `./gradlew app:dependencies`
- [ ] 빌드 성공 확인: `./gradlew assembleDebug`
- [ ] 디바이스에서 기존 앱 정상 동작 확인

### RN Gradle Plugin을 사용하지 않으면?
| 자동 처리되던 것 | 수동 처리 방법 |
|----------------|--------------|
| JS 번들링 (release) | `react-native bundle` 명령으로 수동 생성 후 assets에 복사 |
| Hermes 바이트코드 컴파일 | `hermesc` CLI로 수동 컴파일 또는 Gradle 커스텀 태스크 |
| Autolinking | 사용할 네이티브 모듈 패키지를 수동 등록 |
| Codegen (New Arch) | TurboModule Spec 수동 빌드 |

### 학습 포인트
- **엔터프라이즈 현실**: 빌드 도구 버전 제약으로 인해 공식 RN 빌드 플러그인을 사용할 수 없는 상황에서의 대안 전략
- `react-android`, `hermes-android`는 Maven Central에 배포되므로 Gradle Plugin 없이도 의존성으로 추가 가능
- RN Gradle Plugin이 자동화하는 작업들을 수동으로 처리하는 방법 이해

### 완료 기준
- `./gradlew assembleDebug` 빌드 성공 (RN 의존성 포함)
- `./gradlew app:dependencies`에서 `react-android`, `hermes-android` 확인 가능
- 기존 Compose UI가 깨지지 않고 정상 동작

---

## Task 3: ReactNativeHost Hilt 싱글톤 구현

### 목표
Hilt DI를 통해 `ReactNativeHost`(또는 New Architecture의 `ReactHost`)를 앱 전역 싱글톤으로 관리하는 기반을 구축한다.

### 배경
브라운필드 앱에서 RN 런타임은 반드시 싱글톤이어야 한다. Hermes 엔진을 중복 로드하면 메모리가 폭증하고, ReactInstanceManager가 여러 개면 JS 컨텍스트 간 상태 공유가 불가능하다. 현재 `AppModule.kt`에 빈 Hilt 모듈이 준비되어 있다.

### 작업 항목

- [ ] **New Architecture 여부에 따른 분기 결정**
  - `newArchEnabled=true`(Fabric/TurboModules) → `ReactHost` + `ReactSurfaceView` 사용
  - `newArchEnabled=false`(Bridge) → `ReactNativeHost` + `ReactRootView` 사용
  - RN 0.83.1의 기본값과 브라운필드에서의 권장 설정 확인
- [ ] **AppModule.kt에 Provider 구현**
  - `@Provides @Singleton`으로 ReactNativeHost(또는 ReactHost) 제공
  - `getUseDeveloperSupport()` → `BuildConfig.DEBUG`
  - `getPackages()` → `PackageList(application).packages`
  - `getJSMainModuleName()` → `"index"` (RnApp의 index.js 진입점)
  - `getBundleAssetName()` → `"index.android.bundle"` (Release 번들 파일명)
- [ ] **MainApplication.kt 수정**
  - 필요 시 `ReactApplication` 인터페이스 구현
  - Hilt가 관리하는 ReactNativeHost와 Application 클래스의 관계 정립
- [ ] **초기화 타이밍 전략 결정**
  - Lazy 초기화 (기본, RN 화면 진입 시) vs Eager 초기화 (앱 시작 시 백그라운드)
  - Lazy의 장점: 앱 콜드 스타트 영향 없음
  - Lazy의 단점: RN 화면 최초 진입 시 1~3초 지연
- [ ] 빌드 및 앱 실행 확인 (RN 화면 없이도 크래시 없이 동작)

### 학습 포인트
- ReactNativeHost vs ReactHost (New Architecture)의 차이
- ReactInstanceManager의 역할: JS 엔진 관리, 번들 로딩, 네이티브 모듈 레지스트리
- Hilt의 `SingletonComponent` 스코프와 Application 생명주기의 관계
- 브라운필드에서 ReactApplication 인터페이스 구현의 필요성과 대안

### 완료 기준
- 앱 빌드 및 실행 시 Hilt injection 에러 없음
- Logcat에서 ReactNativeHost 또는 ReactHost 싱글톤 생성 로그 확인 가능
- 기존 5개 탭 네비게이션 정상 동작

---

## Task 4: SettingsScreen에 RN 뷰 삽입 (POC 핵심)

### 목표
Compose의 `AndroidView`로 `ReactRootView`(또는 `ReactSurfaceView`)를 래핑하여, 설정 탭에서 RN으로 구현된 "Hello from React Native" 화면을 렌더링한다.

### 배경
이것이 브라운필드 통합의 핵심 순간이다. Compose 선언형 UI 안에 RN이라는 imperative View를 삽입하면서, 생명주기 동기화, 메모리 관리, 상태 유지를 모두 올바르게 처리해야 한다.

### 작업 항목

- [ ] **RN 측 설정 화면 컴포넌트 작성**
  - `apps/RnApp/` 내에 Settings 화면 컴포넌트 생성 (간단한 텍스트/버튼)
  - `index.js`에 `AppRegistry.registerComponent('SettingsModule', () => SettingsApp)` 등록
  - 기존 `RnApp` 등록은 유지 (멀티 Surface 기반 마련)
- [ ] **재사용 가능한 ReactNativeView Composable 작성**
  - `AndroidView`의 `factory`에서 ReactRootView 생성 및 `startReactApplication()` 호출
  - `onRelease`에서 `unmountReactApplication()` 호출
  - `DisposableEffect` + `LifecycleEventObserver`로 생명주기 동기화
    - `ON_RESUME` → `reactInstanceManager.onHostResume(activity)`
    - `ON_PAUSE` → `reactInstanceManager.onHostPause(activity)`
    - `ON_DESTROY` → `reactInstanceManager.onHostDestroy(activity)`
  - `remember`로 ReactRootView 인스턴스 캐싱 (recomposition 시 재생성 방지)
- [ ] **SettingsScreen.kt 수정**
  - 기존 Text("설정") 대신 ReactNativeView Composable 사용
  - Hilt를 통해 ReactNativeHost 주입 (`hiltViewModel` 또는 직접 injection)
  - moduleName: `"SettingsModule"` 전달
- [ ] **Dev 환경 번들 로딩 확인**
  - Metro 서버 실행: `npx nx start RnApp`
  - `adb reverse tcp:8081 tcp:8081` 포트 포워딩
  - AndroidApp 실행 → 설정 탭 진입 → RN 화면 렌더링 확인
- [ ] **기본 검증**
  - 설정 탭 진입/이탈 반복 5회 → 크래시 없음
  - 홈 → 설정 → 홈 → 설정 탭 전환 시 RN 뷰 정상 복원
  - 앱 백그라운드/포그라운드 전환 후 RN 뷰 정상 동작

### 주의 사항
- `AppNavigation.kt`의 `restoreState = true` 설정과 RN 뷰 상태 복원의 상호작용 확인
- Metro 서버 없이 앱을 실행하면 RedBox/크래시가 발생할 수 있으므로, Dev 빌드에서 Metro 미연결 시 폴백 UI 표시 고려
- New Architecture(Fabric) 모드에서는 `ReactRootView` 대신 `ReactSurfaceView` 사용 필요 여부 확인

### 학습 포인트
- Compose `AndroidView`의 동작 원리: `factory`(1회 실행) vs `update`(recomposition마다) vs `onRelease`(제거 시)
- ReactRootView의 생명주기: `startReactApplication()` → JS 로딩 → 렌더링 → `unmountReactApplication()`
- `AppRegistry.registerComponent`로 다중 RN 진입점(Surface) 등록하는 패턴
- Metro 개발 서버와 네이티브 앱의 통신 방식 (HTTP로 JS 번들 전달)

### 완료 기준
- 설정 탭 진입 시 "Hello from React Native" (또는 동등한 RN 화면) 렌더링
- 탭 전환 시 크래시/메모리 릭 없음
- Metro Hot Reload로 RN 코드 변경 시 설정 탭에 즉시 반영

---

## Task 5: Native <-> RN 양방향 통신 구현

### 목표
네이티브(Kotlin)와 RN(TypeScript) 간에 데이터를 주고받는 통신 채널을 구축한다.

### 배경
실제 엔터프라이즈 앱에서는 네이티브 측의 인증 토큰, 사용자 정보, 설정값 등을 RN 화면에 전달하고, RN에서의 사용자 액션(버튼 클릭 등)을 네이티브 네비게이션으로 전달해야 한다.

### 작업 항목

- [ ] **Native → RN: 초기 데이터 전달 (initialProperties)**
  - `startReactApplication()`의 세 번째 인자 `Bundle`에 데이터 담기
  - RN 측에서 `props`로 수신하여 화면에 표시
  - 예시: 사용자 이름, 앱 버전, 테마 설정 등
- [ ] **RN → Native: NativeModule 메서드 호출**
  - 커스텀 NativeModule 작성 (Bridge 방식)
    - Kotlin: `ReactContextBaseJavaModule` 상속, `@ReactMethod` 어노테이션
    - 예시: `navigateToNativeScreen(screenName: String)` → Compose Navigation 호출
  - ReactPackage 생성 및 ReactNativeHost의 `getPackages()`에 등록
  - RN 측에서 `NativeModules.NavigationModule.navigateToNativeScreen('home')` 호출
- [ ] **Native → RN: 이벤트 전송 (DeviceEventEmitter)**
  - 네이티브에서 `reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java).emit(...)` 호출
  - RN 측에서 `DeviceEventEmitter.addListener()`로 수신
  - 예시: 네이티브 설정 변경 시 RN에 알림
- [ ] **통신 테스트**
  - RN 화면의 버튼 클릭 → 네이티브 Toast 표시
  - 네이티브에서 전달한 사용자 이름이 RN 화면에 표시
  - 양방향 데이터 흐름이 정상 동작하는지 확인

### (선택) TurboModule 방식 도전

- [ ] TurboModule Spec(TypeScript)을 정의하여 Codegen이 네이티브 인터페이스 자동 생성하도록 구성
- [ ] Bridge 방식과 성능/개발 경험 비교

### 학습 포인트
- Bridge 아키텍처: JS → Bridge(JSON 직렬화) → Native → Bridge(JSON 역직렬화) → JS
- TurboModule: JSI(JavaScript Interface)를 통한 직접 호출, 직렬화 오버헤드 제거
- `ReactApplicationContext`의 역할과 네이티브 모듈에서의 Activity 접근 방법
- 네이티브 이벤트 시스템(DeviceEventEmitter)과 React의 이벤트 핸들링

### 완료 기준
- RN 화면의 버튼 클릭으로 네이티브 측 동작 트리거 성공
- 네이티브에서 전달한 데이터가 RN 화면에 정상 표시
- 양방향 통신이 안정적으로 동작 (반복 호출 시 크래시/메모리 릭 없음)

---

## Task 6: Release 빌드 및 번들링

### 목표
Dev 환경(Metro 서버)이 아닌 Release 환경에서 JS 번들이 APK에 포함되어 독립 실행되도록 빌드 파이프라인을 구성한다.

### 배경
프로덕션에서는 Metro 서버가 없으므로, JS 번들을 사전에 컴파일하여 APK의 `assets/`에 포함해야 한다. Hermes를 사용하면 바이트코드(.hbc)로 사전 컴파일되어 런타임 파싱 시간이 대폭 감소한다.

### 작업 항목

- [ ] **Release 빌드 시 JS 번들 자동 생성 확인**
  - Gradle의 `bundleReleaseJsAndAssets` 태스크가 정상 동작하는지 확인
  - `react {}` 블록의 `entryFile`이 올바른 경로(`../RnApp/index.js`)를 가리키는지 확인
  - 생성된 번들 위치: `app/build/generated/assets/createBundleReleaseJsAndAssets/index.android.bundle`
- [ ] **Hermes 바이트코드 컴파일 확인**
  - Release 빌드에서 `.bundle`이 아닌 Hermes 바이트코드(`.hbc`)로 변환되는지 확인
  - 번들 파일을 hexdump로 검사하여 Hermes 매직 넘버 확인
- [ ] **Release APK 검증**
  - `./gradlew assembleRelease` 실행
  - APK 내 `assets/index.android.bundle` 포함 여부 확인
  - APK 크기 측정 (RN 추가 전 baseline과 비교)
- [ ] **ProGuard/R8 규칙 추가**
  - `proguard-rules.pro`에 RN 관련 keep 규칙 추가
  - `com.facebook.react.**`, `com.facebook.hermes.**`, `com.facebook.jni.**` 유지
  - Release 빌드 후 난독화된 상태에서 RN 화면 정상 동작 확인
- [ ] **Release 빌드 실행 테스트**
  - Metro 서버 없이 Release APK 설치 후 설정 탭의 RN 화면 정상 렌더링 확인

### 학습 포인트
- Metro 번들러의 역할: JS 모듈 해석 → 트리 쉐이킹 → 단일 번들 생성
- Hermes 바이트코드 사전 컴파일과 런타임 성능 이점 (파싱 시간 70~80% 감소)
- RN Gradle Plugin이 Release 빌드 시 자동 수행하는 태스크 체인
- ProGuard/R8이 RN 네이티브 코드에 미치는 영향과 keep 규칙의 필요성

### 완료 기준
- `./gradlew assembleRelease` 성공
- Release APK에서 Metro 서버 없이 RN 화면 정상 렌더링
- APK 크기 증가분 측정 및 기록

---

## Task 7: 멀티 Surface 확장 및 안정화

### 목표
설정 탭 외에 추가 화면(예: 채팅 탭)도 RN으로 전환하여, 단일 ReactInstanceManager + 다중 ReactRootView 패턴의 실제 동작을 검증한다.

### 배경
실제 엔터프라이즈 앱에서는 여러 화면을 점진적으로 RN으로 전환한다. 이때 ReactInstanceManager를 공유하면서 각 화면은 독립적인 ReactRootView(Surface)로 동작해야 한다.

### 작업 항목

- [ ] **RN 측 추가 화면 등록**
  - 채팅 화면 컴포넌트 작성 (Settings와 다른 UI)
  - `index.js`에 `AppRegistry.registerComponent('ChatModule', () => ChatApp)` 추가
- [ ] **ChatScreen.kt 수정**
  - 기존 Text("채팅") 대신 ReactNativeView Composable 사용 (`moduleName: "ChatModule"`)
- [ ] **다중 Surface 동시 동작 검증**
  - 설정 탭 → 채팅 탭 → 설정 탭 빠르게 전환 반복
  - 두 RN 화면의 상태가 독립적으로 유지되는지 확인
  - 메모리 사용량 모니터링 (Surface 추가 시 증가분 측정)
- [ ] **RN 화면 간 공유 상태 구현 (선택)**
  - JS 전역 스토어(Zustand 등)로 Surface 간 상태 공유 실험
  - 네이티브 → JS 이벤트로 화면 전환 알림 전달
- [ ] **안정화 테스트**
  - 5개 탭 빠른 전환 50회 반복 → 크래시/ANR 없음
  - 화면 회전 시 RN 뷰 정상 재렌더링
  - `Don't Keep Activities` 옵션 활성화 후 탭 전환 테스트
  - 메모리 프로파일링: 10분간 반복 사용 후 힙 덤프 분석

### 학습 포인트
- 단일 ReactInstanceManager(Hermes 엔진) 위에서 다중 Surface가 독립 동작하는 구조
- `AppRegistry.registerComponent`로 다중 진입점을 등록하고 네이티브에서 선택 호출하는 패턴
- Surface 간 JS 메모리 공유 vs 격리의 이해
- 브라운필드 앱에서의 성능 프로파일링 방법

### 완료 기준
- 2개 이상의 탭이 RN 화면으로 동작
- 탭 전환 50회 반복 시 크래시/메모리 릭 없음
- 각 Surface의 상태가 독립적으로 유지

---

## Task 8: 성능 Baseline 측정 및 최적화

### 목표
RN 통합 전후의 성능 지표를 측정하고, 엔터프라이즈 환경에서 수용 가능한 수준인지 평가한다.

### 측정 항목

| 지표 | 측정 방법 | 목표값 |
|------|----------|--------|
| APK 크기 증가 | RN 통합 전후 APK 크기 비교 | < 15MB 증가 |
| 앱 콜드 스타트 | adb shell am start + Displayed 로그 | RN 추가 후 < 500ms 증가 |
| RN 화면 최초 렌더링 | RN 화면 진입 → 첫 paint 시간 | < 2초 |
| RN 화면 재진입 | 캐싱된 RN 화면 복귀 시간 | < 300ms |
| 메모리 사용량 | Android Profiler 힙 측정 | RN 활성 시 < +80MB |
| 탭 전환 프레임 드롭 | Systrace/GPU Profiler | 드롭 프레임 < 5% |

### 작업 항목

- [ ] **Baseline 측정 (RN 통합 전)**: Task 1 이전에 APK 크기, 콜드 스타트, 메모리 측정
- [ ] **통합 후 측정**: Task 6 완료 후 동일 지표 재측정
- [ ] **최적화 적용** (측정 결과에 따라)
  - Hermes 바이트코드 사전 컴파일 확인
  - Metro의 `inline-requires` 변환 활성화
  - 불필요한 RN 의존성 제거 (번들 크기 감소)
  - ReactInstanceManager의 `createReactContextInBackground()` 선행 초기화 (필요 시)
- [ ] **측정 결과 문서화**

### 학습 포인트
- 브라운필드 RN 통합이 네이티브 앱 성능에 미치는 실질적 비용
- Hermes 바이트코드, inline-requires, RAM 번들 등 최적화 기법
- Android 성능 측정 도구(Systrace, Android Profiler, Macrobenchmark) 활용법

### 완료 기준
- RN 통합 전후 성능 지표가 문서화됨
- 목표값을 초과하는 항목에 대해 최적화 적용 또는 원인 분석 완료

---

## 부록: 주의사항 체크리스트

### 빌드 관련
- [ ] CI 환경에 Node.js >= 20, JDK 17, Android SDK, NDK 설치 확인
- [ ] `npm ci` (루트)가 Gradle 빌드 전에 실행되어야 함
- [ ] Metro의 `watchFolders`에서 `apps/AndroidApp` 내부 Gradle 파일 감시를 제외하도록 `blockList` 설정 고려

### 안정성 관련
- [ ] `AndroidManifest.xml`에 `android:usesCleartextTraffic="true"` 추가 (Debug 빌드에서 Metro 연결용)
- [ ] Release 빌드 시 ProGuard keep 규칙 반드시 검증
- [ ] 화면 회전 대응: `android:configChanges` 설정 검토

### 아키텍처 관련
- [ ] RN 화면 내부 네비게이션은 React Navigation이 소유, RN <-> 네이티브 전환은 단일 진입/종료 포인트
- [ ] Navigation 경계에서 Deep Link 라우팅 이중 구조 주의
- [ ] Android 하드웨어 백 버튼 처리: Compose Navigation + React Navigation 이중 관리

### 엔터프라이즈 적용 시 추가 고려
- [ ] 탈출 전략(exit strategy) 유지: RN 화면을 네이티브로 되돌릴 수 있는 추상화 레이어
- [ ] 통합 크래시 리포팅 (Sentry RN SDK: 네이티브 + JS 크래시 단일 대시보드)
- [ ] 빌드 설정 변경 시 양 팀(네이티브/RN) 리뷰 필수

---

> Phase 2 (iOS 통합)는 Phase 1의 모든 Task 완료 후 별도 문서로 작성 예정.
