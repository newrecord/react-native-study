# Phase 1: Android 브라운필드 RN 통합 학습 태스크

> 대규모 Android 네이티브 앱(Jetpack Compose + Hilt)에 React Native를 브라운필드 방식으로 통합하기 위한 단계별 학습 가이드.
> 각 Task는 순서대로 진행하며, 이전 Task가 완료되어야 다음으로 넘어갈 수 있다.

---

## Task 1: 빌드 환경 버전 통일 [완료]

### 목표
AndroidApp의 빌드 도구 버전을 실제 프로젝트 환경과 동일하게 맞추고, RN 통합이 가능한 수준으로 구성한다.

### 실제 적용된 변경 (최종)

| 항목 | 변경 전 | 변경 후 | 비고 |
|------|--------|--------|------|
| Gradle Wrapper | 8.9 | **8.11.1** | 실제 프로젝트 환경과 동일 |
| AGP | 8.7.3 | **8.10.1** | 실제 프로젝트 환경과 동일 |
| Kotlin | 2.0.21 | **2.1.0** | 실제 프로젝트 환경과 동일 |
| KSP | 2.0.21-1.0.28 | **2.1.0-1.0.29** | Kotlin 버전 매칭 |
| compileSdk | 35 | **36** | |
| targetSdk | 35 | **35** | 실제 프로젝트 환경과 동일 |
| NDK | 미설정 | **27.1.12297006** | RN 네이티브 모듈 빌드용 |
| Hilt | 2.54 | **2.53.1** | 실제 프로젝트 환경과 동일 |
| Compose BOM | 2024.12.01 | **2025.04.00** | 실제 프로젝트 환경과 동일 |
| Node.js | >=18 | **>=20** | RN 요구사항 |

### 학습 포인트
- **AGP-Gradle 호환성 매트릭스**: Gradle 버전이 AGP 범위를 결정하고, AGP가 RN Gradle Plugin 사용 가능 여부를 좌우한다.
- **RN 버전별 Gradle Plugin AGP 요구사항**:
  - RN 0.76.x → AGP 8.6.0, RN 0.77.x → AGP 8.7.2, RN 0.78.x → AGP 8.8.0
  - RN 0.79.x → AGP 8.8.2, RN 0.80.x → AGP 8.9.2, RN 0.83.x → AGP 8.12.0
- Kotlin/KSP 버전은 Gradle 버전과 독립적으로 업데이트 가능하다.

### 완료 기준 [달성]
- [x] AndroidApp 빌드 성공 (`./gradlew assembleDebug`)
- [x] 디바이스에서 앱 실행 및 정상 동작 확인
- [x] RnApp 빌드에 영향 없음

---

## Task 2: AndroidApp Gradle에 React Native 의존성 추가 [완료]

### 목표
AndroidApp의 Gradle 빌드 설정에 React Native 의존성과 RN Gradle Plugin을 추가한다.

### 실제 적용된 변경 (최종)

- [x] **settings.gradle.kts**
  - `includeBuild("../../node_modules/@react-native/gradle-plugin")` — RN Gradle Plugin 포함
  - `id("com.facebook.react.settings")` — settings plugin 적용 (autolinking.json 생성)
  - `autolinkLibrariesFromCommand(workingDirectory = file("../RnApp"))` — 모노레포 경로 설정
  - `repositoriesMode` → `PREFER_PROJECT` (RN Plugin이 추가하는 Maven repo 허용)
- [x] **app/build.gradle.kts**
  - `id("com.facebook.react")` 플러그인 적용
  - `react {}` 블록으로 모노레포 경로 설정 (root, reactNativeDir, codegenDir, cliFile)
  - `react-android:0.79.3`, `hermes-android:0.79.3` 의존성 추가
  - `buildConfig = true`, `jniLibs.useLegacyPackaging = true`
  - `ndk.abiFilters += listOf("armeabi-v7a", "arm64-v8a")`
- [x] **libs.versions.toml**: `reactAndroid = "0.79.3"`, `hermesAndroid = "0.79.3"` 등록
- [x] **RnApp/package.json**: RN 0.83.1 → **0.79.3** 다운그레이드 (실제 프로젝트와 동일)

### 학습 포인트
- **RN Gradle Plugin의 역할**: autolinking(PackageList 생성), codegen(libappmodules.so), JS 번들링, Hermes 컴파일을 자동 처리
- **settings plugin**: `autolinkLibrariesFromCommand()`로 `autolinking.json`을 생성. Gradle 빌드 전 설정 단계에서 실행됨
- **모노레포 경로 설정**: `react {}` 블록에서 `root`, `reactNativeDir`, `codegenDir`, `cliFile`을 모노레포 구조에 맞게 상대경로로 지정해야 함
- **hermes-android는 명시적 의존성 필요**: RN 0.79.x에서 Gradle Plugin이 Hermes를 자동 추가하지 않음. `com.facebook.react:hermes-android:0.79.3`을 직접 추가해야 함

### 완료 기준 [달성]
- [x] `./gradlew assembleDebug` 빌드 성공 (autolinking, codegen, CMake 포함)
- [x] APK에 `libappmodules.so`, `libhermes.so`, `libreactnative.so` 포함 확인
- [x] 기존 Compose UI 정상 동작

---

## Task 3: ReactHost Hilt 싱글톤 구현 (New Architecture) [완료]

### 목표
Hilt DI를 통해 `ReactHost`(New Architecture)를 앱 전역 싱글톤으로 관리하는 기반을 구축한다.

### 배경
브라운필드 앱에서 RN 런타임은 반드시 싱글톤이어야 한다. Hermes 엔진을 중복 로드하면 메모리가 폭증하고, JS 컨텍스트 간 상태 공유가 불가능하다.

### 실제 적용된 변경 (최종)

- [x] **MainApplication.kt** — `@HiltAndroidApp` + `ReactApplication` 구현
  - `DefaultReactNativeHost` 사용 (패키지 목록, JS 엔트리, 개발 모드 등 설정 담당)
  - `reactHost` = `getDefaultReactHost(applicationContext, reactNativeHost)` — New Architecture 런타임
  - `PackageList(this).packages` — Gradle Plugin의 autolinking이 생성한 패키지 목록
  - `SoLoader.init(this, OpenSourceMergedSoMapping)` — 병합된 .so 매핑으로 초기화
  - `DefaultNewArchitectureEntryPoint.load()` — Fabric + TurboModules 네이티브 코드 초기화
  - `BuildConfig.IS_NEW_ARCHITECTURE_ENABLED` / `IS_HERMES_ENABLED` — Gradle Plugin이 자동 생성
- [x] **AppModule.kt** — `@Provides @Singleton`으로 `ReactHost` 제공
- [x] **AndroidManifest.xml** — `INTERNET` 권한, `usesCleartextTraffic=true`
- [x] **gradle.properties** — `newArchEnabled=true`, `hermesEnabled=true`

### 학습 포인트
- **ReactNativeHost vs ReactHost**: `ReactNativeHost`는 설정(패키지, JS 엔트리 등) 담당, `ReactHost`는 New Architecture 런타임(Fabric + TurboModules) 담당. 둘 다 필요함.
- **`DefaultReactNativeHost`**: `isNewArchEnabled`, `isHermesEnabled` property를 제공하는 확장 클래스
- **`OpenSourceMergedSoMapping`**: RN 0.76+에서 개별 .so 파일들이 `libreactnative.so`로 병합됨. 이 매핑 없이 `SoLoader.init()`하면 `libXXX.so not found` 크래시 발생
- **`DefaultNewArchitectureEntryPoint.load()`**: `libappmodules.so`를 로드하여 TurboModules 등록. Gradle Plugin이 생성한 autolinking 네이티브 코드 초기화
- **`PackageList`**: Gradle Plugin의 autolinking이 빌드 시 자동 생성하는 클래스. 수동으로 `MainReactPackage()` 등록 불필요

### 완료 기준 [달성]
- [x] 앱 빌드 및 실행 시 Hilt injection 에러 없음
- [x] 크래시/에러 로그 없음
- [x] 기존 5개 탭 네비게이션 정상 동작

---

## Task 4: SettingsScreen에 RN 뷰 삽입 (POC 핵심) [완료]

### 목표
Compose의 `AndroidView`로 React Native 화면을 래핑하여, 설정 탭에서 RN으로 구현된 화면을 렌더링한다.

### 배경
브라운필드 통합의 핵심. Compose 선언형 UI 안에 RN이라는 imperative View를 삽입하면서, 생명주기 동기화와 New Architecture의 Fabric 렌더링을 올바르게 처리해야 한다.

### 실제 적용된 변경

- [x] **RN 측 설정 화면 컴포넌트** (`apps/RnApp/src/screens/SettingsScreen.tsx`)
  - 간단한 텍스트 카드 UI ("이 화면은 Jetpack Compose 앱 안에서 React Native로 구현된 화면입니다")
- [x] **RN 진입점 등록** (`apps/RnApp/index.js`)
  - `AppRegistry.registerComponent('SettingsModule', () => SettingsScreen)` 추가
  - 기존 `RnApp` 등록 유지 (멀티 Surface 기반)
- [x] **ReactNativeView Composable** (`ui/components/ReactNativeView.kt`)
  - `ReactHost.createSurface(context, moduleName, null)` → Fabric Surface 생성
  - `surface.start()` / `surface.stop()` → `DisposableEffect`에서 생명주기 관리
  - `ReactHost.onHostResume/Pause/Destroy` → `LifecycleEventObserver`로 동기화
  - `AndroidView(factory = { surface.view!! })` → Compose에 삽입
- [x] **SettingsScreen.kt** → `ReactNativeView(reactHost, "SettingsModule")`
- [x] **DI 체인**: `AppModule` → `ReactHost` → `MainActivity` → `AppNavigation` → `SettingsScreen` → `ReactNativeView`
- [x] 디바이스에서 설정 탭 진입 시 RN 화면 정상 렌더링 확인

### 해결한 핵심 이슈

#### 1. New Architecture에서 ReactInstanceManager + ReactRootView 사용 불가
- **증상**: `Could not invoke UIManager.createView null`, `Root node with tag doesn't exist`
- **원인**: `newArchEnabled=true`일 때 UIManager가 Fabric으로 교체됨. `ReactInstanceManager` + `ReactRootView.startReactApplication()`은 레거시 UIManager 경로를 사용하여 null 반환
- **해결**: **ReactHost + ReactSurface API**로 전환
  ```
  ReactInstanceManager + ReactRootView (Bridge API) ❌
  ReactHost + ReactSurface (New Architecture API) ✅
  ```

#### 2. libhermes.so 로딩 실패
- **증상**: `dlopen failed: library "/vendor/lib64/libhermes.so" not found` → 앱 즉시 크래시
- **원인**: RN 0.79.3에서 Gradle Plugin이 Hermes 의존성을 자동 추가하지 않음
- **해결**: `com.facebook.react:hermes-android:0.79.3` 명시적 의존성 추가

#### 3. @react-native/new-app-screen 미존재
- **증상**: Metro 500 에러 (`Unable to resolve module @react-native/new-app-screen`)
- **원인**: RN 0.83.1 전용 패키지. RN 0.79.3에 없음
- **해결**: `App.tsx`에서 해당 import 제거, 간단한 컴포넌트로 교체

### 학습 포인트 (중요)

#### New Architecture 브라운필드 통합의 핵심 API 선택
| API | 아키텍처 | Compose 통합 방식 |
|-----|---------|-----------------|
| `ReactInstanceManager` + `ReactRootView` | Bridge (레거시) | `AndroidView(factory = { rootView })` |
| **`ReactHost` + `ReactSurface`** | **New Architecture** | **`AndroidView(factory = { surface.view!! })`** |

- **`ReactHost.createSurface(context, moduleName, initialProps)`**: Fabric Surface를 생성. 이 Surface가 내부적으로 Fabric 렌더러를 통해 UI를 그림
- **`ReactSurface.start()`**: JS 런타임 시작 및 렌더링 개시 (비동기 TaskInterface 반환)
- **`ReactSurface.stop()`**: 렌더링 중지 및 리소스 해제
- **`ReactSurface.view`**: Compose `AndroidView`에 삽입할 실제 Android View

#### Compose + RN 생명주기 동기화
```
Compose LifecycleOwner ─── LifecycleEventObserver ──→ ReactHost
  ON_RESUME  ──→ reactHost.onHostResume(activity)
  ON_PAUSE   ──→ reactHost.onHostPause(activity)
  ON_DESTROY ──→ reactHost.onHostDestroy(activity)

DisposableEffect ──→ surface.start() / surface.stop()
```

#### 모노레포에서 RN Gradle Plugin 경로 설정
```
// settings.gradle.kts (AndroidApp 루트 기준)
includeBuild("../../node_modules/@react-native/gradle-plugin")
autolinkLibrariesFromCommand(workingDirectory = file("../RnApp"))

// app/build.gradle.kts (app/ 모듈 기준)
react {
    root = file("../../RnApp")                                    // RN JS 프로젝트
    reactNativeDir = file("../../../node_modules/react-native")   // react-native 패키지
    codegenDir = file("../../../node_modules/@react-native/codegen")
    cliFile = file("../../../node_modules/@react-native-community/cli/build/bin.js")
}
```

### 완료 기준 [달성]
- [x] 설정 탭 진입 시 RN 화면 ("설정 — React Native에서 렌더링됨") 정상 렌더링
- [x] 앱 실행 시 크래시 없음
- [x] Metro 서버에서 JS 번들 정상 로딩

---

## Task 5: Native <-> RN 양방향 통신 구현 [완료]

### 목표
네이티브(Kotlin)와 RN(TypeScript) 간에 데이터를 주고받는 통신 채널을 구축한다.

### 배경
실제 엔터프라이즈 앱에서는 네이티브 측의 인증 토큰, 사용자 정보, 설정값 등을 RN 화면에 전달하고, RN에서의 사용자 액션(버튼 클릭 등)을 네이티브 네비게이션으로 전달해야 한다.

### 실제 적용된 변경

- [x] **Native → RN: 초기 데이터 전달 (initialProperties)**
  - `ReactNativeView` Composable에 `initialProperties: Bundle?` 파라미터 추가
  - `ReactHost.createSurface(context, moduleName, initialProperties)`로 데이터 전달
  - `SettingsScreen.kt`에서 Bundle에 `userName`, `appVersion`, `themeName` 담아 전달
  - RN 측에서 `props.userName` 등으로 수신하여 화면에 표시
- [x] **RN → Native: NativeModule 메서드 호출**
  - `bridge/AppBridgeModule.kt` — `ReactContextBaseJavaModule` 상속, Bridge 방식
    - `@ReactMethod showToast(message)` → Android Toast 표시
    - `@ReactMethod navigateToNativeScreen(screenName)` → SharedFlow로 Compose Navigation에 전달
    - `@ReactMethod getAppInfo(promise)` → Promise 기반 앱 정보 반환
  - `bridge/AppBridgePackage.kt` — ReactPackage 구현
  - `MainApplication.kt`의 `getPackages()`에 `AppBridgePackage()` 등록
  - RN 측에서 `NativeModules.AppBridge.showToast(...)` 등으로 호출
- [x] **Native → RN: 이벤트 전송 (DeviceEventEmitter)**
  - `AppBridgeModule.sendEvent()` → `RCTDeviceEventEmitter.emit()` 호출
  - `@ReactMethod requestThemeChange(themeName)` → 네이티브 처리 후 `onThemeChanged` 이벤트 전송
  - RN 측에서 `DeviceEventEmitter.addListener('onThemeChanged', ...)` 로 수신
  - `addListener`/`removeListeners` 경고 방지용 메서드 추가
- [x] **통신 테스트 UI 구현**
  - RN `SettingsScreen.tsx` — 3개 섹션으로 구성:
    1. 초기 데이터 표시 카드 (userName, appVersion, themeName)
    2. RN → Native 버튼 (Toast, 홈 이동, 앱 정보 조회)
    3. Native → RN 이벤트 (테마 변경 요청/수신 결과 표시)
- [x] **RN → Compose Navigation 연동**
  - `AppBridgeModule.companion.navigationEvents` (MutableSharedFlow)
  - `AppNavigation.kt`에서 `LaunchedEffect`로 collect → `navController.navigate()` 호출

### 학습 포인트

#### Bridge NativeModule 작성 패턴
- `ReactContextBaseJavaModule` 상속 → `getName()` = JS 측 모듈 이름
- `@ReactMethod` 어노테이션으로 JS에 노출할 메서드 정의
- `ReactPackage` 구현 → `MainApplication.getPackages()`에 등록
- JS 측: `NativeModules.모듈이름.메서드이름()` 으로 호출

#### 세 가지 통신 채널 비교
| 채널 | 방향 | 타이밍 | 용도 |
|------|------|--------|------|
| `initialProperties` (Bundle) | Native → RN | Surface 생성 시 1회 | 초기 설정, 사용자 정보 |
| `NativeModule @ReactMethod` | RN → Native | RN에서 필요할 때 호출 | Toast, 네비게이션, API 호출 |
| `DeviceEventEmitter` | Native → RN | 네이티브에서 필요할 때 발행 | 상태 변경 알림, 실시간 이벤트 |

#### NativeModule에서 Compose Navigation 연동 패턴
```
RN (JS Thread)
  → NativeModules.AppBridge.navigateToNativeScreen("home")
    → AppBridgeModule.navigateToNativeScreen()
      → MutableSharedFlow.tryEmit("home")
        → AppNavigation LaunchedEffect collect
          → navController.navigate("home")
```
- NativeModule은 ReactApplicationContext만 접근 가능, NavController에 직접 접근 불가
- Kotlin SharedFlow를 이벤트 버스로 활용하여 Bridge Layer → Compose Layer 통신

#### Promise 기반 비동기 통신
- `@ReactMethod fun getAppInfo(promise: Promise)` — 마지막 파라미터가 Promise면 RN에서 async/await 사용 가능
- `promise.resolve(WritableMap)` / `promise.reject(code, message)` 로 결과 반환
- `Arguments.createMap()` 으로 RN에 전달할 데이터 구조 생성

#### DeviceEventEmitter 사용 시 주의사항
- `addListener(eventName)`와 `removeListeners(count)` 메서드를 빈 구현으로 추가해야 경고 방지
- `reactApplicationContext.getJSModule(RCTDeviceEventEmitter::class.java).emit()` 으로 이벤트 발행
- RN 측에서 `useEffect` cleanup으로 `subscription.remove()` 호출 필수 (메모리 릭 방지)

### 완료 기준 [달성]
- [x] RN 화면의 버튼 클릭으로 네이티브 측 동작 트리거 성공 (Toast, Navigation)
- [x] 네이티브에서 전달한 데이터가 RN 화면에 정상 표시 (initialProperties)
- [x] 양방향 통신이 안정적으로 동작 (빌드 성공, 반복 호출 가능한 구조)

---

## Task 6: Release 빌드 및 번들링 [완료]

### 목표
Dev 환경(Metro 서버)이 아닌 Release 환경에서 JS 번들이 APK에 포함되어 독립 실행되도록 빌드 파이프라인을 구성한다.

### 배경
프로덕션에서는 Metro 서버가 없으므로, JS 번들을 사전에 컴파일하여 APK의 `assets/`에 포함해야 한다. Hermes를 사용하면 바이트코드(.hbc)로 사전 컴파일되어 런타임 파싱 시간이 대폭 감소한다.

### 실제 적용된 변경

- [x] **Release 빌드 시 JS 번들 자동 생성 확인**
  - `createBundleReleaseJsAndAssets` 태스크 정상 동작 확인
  - `react {}` 블록의 `root = file("../../RnApp")`으로 모노레포 경로 설정 (entryFile은 자동 해석)
  - 생성된 번들: `app/build/intermediates/assets/release/mergeReleaseAssets/index.android.bundle` (857KB)
- [x] **Hermes 바이트코드 컴파일 확인**
  - hexdump 매직 넘버 `c61fbc03` — Hermes 바이트코드 포맷 확인 (JS 텍스트가 아닌 바이너리)
  - 모노레포에서 hermesc 자동 해석 실패 → `hermesCommand` 명시적 경로 설정으로 해결
- [x] **Release APK 검증**
  - `./gradlew assembleRelease` 성공 (R8 minify + resource shrink)
  - APK 내 `assets/index.android.bundle` (877,516 bytes) 포함 확인
- [x] **ProGuard/R8 규칙 추가**
  - `proguard-rules.pro`에 RN 관련 keep 규칙 추가
  - `com.facebook.react.**`, `com.facebook.hermes.**`, `com.facebook.jni.**`, `com.facebook.soloader.**` 유지
  - `ReactContextBaseJavaModule`의 `@ReactMethod` 리플렉션 보호 규칙 추가
  - R8 난독화 후에도 RN 화면 정상 동작 확인
- [x] **Release 빌드 실행 테스트**
  - Metro 서버 없이 Release APK 설치 후 설정 탭의 RN 화면 정상 렌더링 확인
  - Task 5의 양방향 통신(Toast, Navigation, 이벤트) 모두 정상 동작
- [x] **QA 배포 체계 구축**
  - `debuggableVariants` + `-PbundleInDebug` 플래그로 Debug APK에도 JS 번들 내장
  - Nx 명령어: `build-android-qa`, `build-android-release`, `deploy-qa` 추가

### APK 크기 비교

| 빌드 타입 | APK 크기 | JS 번들 | Metro 필요 | R8 난독화 | 용도 |
|-----------|---------|---------|-----------|----------|------|
| Debug | 76.01 MB | 미포함 | O | X | 로컬 개발 |
| QA Debug | 76.85 MB | 내장 (877KB) | X | X | QA Debug 배포 |
| Release | 12.06 MB | 내장 (Hermes bytecode) | X | O | QA Release / 출시 |

- **Debug → QA Debug 차이**: +0.84 MB (JS 번들 크기만큼 증가)
- **Debug → Release 차이**: -63.95 MB (R8 + 리소스 축소 + debug 심볼 제거)

### 해결한 핵심 이슈

#### 1. 모노레포에서 hermesc 경로 자동 해석 실패
- **증상**: `Couldn't determine Hermesc location. Please set react.hermesCommand`
- **원인**: RN Gradle Plugin이 `%OS-BIN%` 플레이스홀더를 모노레포 구조에서 해석 실패
- **해결**: `react {}` 블록에서 OS 감지 후 명시적 경로 설정
  ```kotlin
  val osDir = if (System.getProperty("os.name").lowercase().contains("mac")) "osx-bin" else "linux64-bin"
  hermesCommand.set(file("../../../node_modules/react-native/sdks/hermesc/$osDir/hermesc").absolutePath)
  ```

#### 2. QA Debug 배포 시 Metro 서버 의존성
- **배경**: 사내 QA 배포 시 Debug/Release 두 바이너리를 모두 전달. Debug APK는 Metro 서버 없이 동작 불가
- **해결**: RN Gradle Plugin의 `debuggableVariants` 프로퍼티 활용
  ```kotlin
  // -PbundleInDebug 전달 시 debug variant도 JS 번들 내장
  if (project.hasProperty("bundleInDebug")) {
      debuggableVariants.set(emptyList())
  }
  ```
- **Nx 명령어**: `npx nx build-android-qa AndroidApp` / `npx nx deploy-qa AndroidApp`

### 학습 포인트

#### RN Gradle Plugin의 빌드 태스크 체인 (Release)
```
assembleRelease
  ├─ createBundleReleaseJsAndAssets (BundleHermesCTask)
  │   ├─ Metro CLI로 JS 번들 생성 (react-native bundle)
  │   ├─ hermesc로 Hermes 바이트코드 컴파일 (.js → .hbc)
  │   └─ 소스맵 생성 (.map)
  ├─ mergeReleaseAssets (번들을 assets/에 배치)
  ├─ minifyReleaseWithR8 (코드 난독화 + 트리 쉐이킹)
  ├─ convertShrunkResourcesToBinaryRelease (리소스 축소)
  └─ packageRelease (최종 APK 패키징)
```

#### `debuggableVariants` 제어 메커니즘
- 기본값: `listOf("debug")` — debug variant는 JS 번들을 생성하지 않음 (Metro 서버 사용)
- `emptyList()` 설정 시 — 모든 variant가 JS 번들 내장 (debug 포함)
- `BundleHermesCTask`는 `debuggableVariants`에 포함되지 않은 variant에만 등록됨

#### ProGuard/R8 필수 keep 규칙
| 규칙 | 이유 |
|------|------|
| `com.facebook.react.**` | RN 코어 — JS Bridge, Fabric, TurboModule 런타임 |
| `com.facebook.hermes.**` | Hermes 엔진 — JNI로 호출되므로 난독화 시 크래시 |
| `com.facebook.jni.**` | JNI 바인딩 — 네이티브 라이브러리 로딩 |
| `com.facebook.soloader.**` | SoLoader — .so 파일 동적 로딩 |
| `@ReactMethod <methods>` | 리플렉션으로 호출 — 난독화 시 메서드명 변경되면 Bridge 호출 실패 |

#### Hermes 바이트코드의 이점
- JS 텍스트 파싱 불필요 → 앱 시작 시간 **70~80% 단축**
- 바이트코드는 JS 소스보다 약간 큰 경우도 있지만, Hermes VM에 최적화된 포맷
- Release 빌드에서 자동 적용 (`hermesEnabled=true` + `BundleHermesCTask`)

#### 빌드 타입별 배포 전략
```
┌──────────────┬────────────┬──────────┬───────────────┐
│ 빌드 타입     │ JS 번들     │ 로그/디버깅 │ 용도           │
├──────────────┼────────────┼──────────┼───────────────┤
│ debug        │ Metro 서버  │ O        │ 개발자 로컬     │
│ QA debug     │ APK 내장    │ O        │ QA Debug 배포  │
│ release      │ APK 내장    │ X        │ QA Release/출시 │
└──────────────┴────────────┴──────────┴───────────────┘
```

### Nx 빌드 명령어 정리

| 명령어 | 용도 |
|--------|------|
| `npx nx build-android AndroidApp` | 개발용 Debug 빌드 |
| `npx nx build-android-qa AndroidApp` | QA Debug 빌드 (JS 번들 내장) |
| `npx nx build-android-release AndroidApp` | Release 빌드 |
| `npx nx deploy-qa AndroidApp` | QA 일괄 실행 (클린→빌드→설치→실행) |

### 완료 기준 [달성]
- [x] `./gradlew assembleRelease` 성공 (R8 minify + resource shrink)
- [x] Release APK에서 Metro 서버 없이 RN 화면 정상 렌더링
- [x] APK 크기: Debug 76.01MB → Release 12.06MB (QA Debug 76.85MB)

---

## Task 6-1: Native ↔ RN 공유 저장소(SharedPreferences) 연동 [완료]

### 목표
네이티브(홈)와 RN(설정) 화면에서 동일한 SharedPreferences를 읽고 쓰는 기능을 구현하여, 브라운필드 앱에서 네이티브와 RN 간 영속 데이터를 공유하는 패턴을 검증한다.

### 배경
실제 엔터프라이즈 앱에서는 네이티브와 RN이 동일한 사용자 설정, 토큰, 캐시 등을 공유해야 한다. SharedPreferences는 가장 기본적인 Android 영속 저장소로, 양쪽에서 자유롭게 접근 가능해야 한다.

### 실제 적용된 변경

- [x] **홈 화면(Compose) — 입력/출력 UI 추가**
  - `HomeScreen.kt`: Column + verticalScroll 레이아웃으로 전환
  - OutlinedTextField + "저장" 버튼 → `SharedPreferences.edit().putString().apply()`
  - Card + "불러오기" 버튼 → `SharedPreferences.getString()`
  - `AppBridgeModule.PREFS_NAME` 상수를 공유하여 동일한 파일명 사용
- [x] **AppBridgeModule — SharedPreferences 접근 메서드 추가**
  - `@ReactMethod saveText(key, value)` → `reactApplicationContext.getSharedPreferences().edit().putString().apply()`
  - `@ReactMethod loadText(key, promise)` → `promise.resolve(prefs.getString(key, null))`
  - `PREFS_NAME = "app_bridge_prefs"` 상수를 companion object에 정의 (네이티브 측에서도 참조)
- [x] **설정 화면(RN) — SharedStorageSection 컴포넌트 추가 (하단)**
  - TextInput + "저장" 버튼 → `AppBridge.saveText(STORAGE_KEY, inputText)`
  - Text 출력 카드 + "불러오기" 버튼 → `await AppBridge.loadText(STORAGE_KEY)`
  - 저장 키: `shared_text` (네이티브/RN 동일)
- [x] **크로스 화면 저장/불러오기 검증**
  - 홈에서 저장 → 설정에서 불러오기 성공
  - 설정에서 저장 → 홈에서 불러오기 성공

### 해결한 이슈

#### Debug 빌드에서 Metro 서버 연결 실패
- **증상**: QA 빌드(JS 번들 내장) 테스트 후 Debug 빌드로 전환 시 RN 화면 미노출
- **원인**: 디바이스의 adb reverse 포트 포워딩이 해제된 상태
- **해결**: `adb reverse tcp:8081 tcp:8081` 실행 후 앱 재시작

### 학습 포인트

#### 네이티브와 RN의 SharedPreferences 공유 패턴
```
Compose (HomeScreen.kt)
  context.getSharedPreferences("app_bridge_prefs", MODE_PRIVATE)
    .edit().putString("shared_text", value).apply()

RN (SettingsScreen.tsx)
  NativeModules.AppBridge.saveText("shared_text", value)
    → AppBridgeModule.saveText()
      → reactApplicationContext.getSharedPreferences("app_bridge_prefs", MODE_PRIVATE)
          .edit().putString("shared_text", value).apply()
```
- 동일한 `PREFS_NAME`과 `key`를 사용하면 네이티브와 RN이 같은 데이터를 읽고 쓸 수 있다
- RN은 NativeModule을 경유해야 하지만, 네이티브는 직접 접근 가능

### 완료 기준 [달성]
- [x] 홈(Compose) ↔ 설정(RN) 간 텍스트 저장/불러오기 정상 동작
- [x] 앱 종료 후 재시작해도 저장된 데이터 유지 (SharedPreferences 영속성)

---

## Task 6-2: RN Surface 탭 전환 시 UI 상태 유지

### 목표
설정 → 홈 → 설정으로 탭을 전환했을 때, RN 화면의 스크롤 위치와 입력 상태가 유지되도록 Surface 생명주기를 개선한다.

### 배경
Task 6-1에서 설정 화면 하단에 입력/출력 폼을 추가하면, 스크롤하여 폼을 확인한 상태에서 홈 탭으로 전환 후 다시 설정 탭으로 돌아오면 RN Surface가 재생성되어 스크롤 위치와 입력값이 초기화되는 문제가 발생할 것으로 예상된다.

### 작업 항목

- [ ] **현상 확인**: 설정 → 홈 → 설정 전환 시 RN UI 초기화 여부 확인
- [ ] **원인 분석**: Compose Navigation의 `saveState`/`restoreState`와 ReactSurface 생명주기 간 상호작용 분석
- [ ] **해결 방안 구현** (예상되는 접근법)
  - ReactSurface를 탭 전환 시 destroy하지 않고 유지 (Composable 생명주기 관리)
  - 또는 RN 측 상태 관리(useState/useRef)로 복원
  - 또는 Compose의 `remember` + Surface 캐싱 전략
- [ ] **검증**: 설정 → 홈 → 설정 전환 후 스크롤 위치, 입력값, 출력값 유지 확인

### 학습 포인트
- Compose Navigation의 백스택 관리와 ReactSurface 생명주기의 불일치
- RN Surface를 persist하기 위한 전략 (Surface 캐싱, View detach/reattach 등)
- 브라운필드 앱에서 RN 화면 상태 유지의 실무적 해결 패턴

### 완료 기준
- 설정 → 홈 → 설정 전환 후 스크롤 위치 유지
- 입력 폼의 텍스트와 출력 폼의 결과가 초기화되지 않음

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
