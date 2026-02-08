---
name: rn-cmd
description: Nx 모노레포 빌드 및 실행 명령어
---

# Nx 모노레포 명령어

이 프로젝트는 Nx 모노레포로 Android(Jetpack Compose + Hilt)와 React Native 앱을 관리합니다.

## Android 앱 (AndroidApp)

### 빌드 (개발용, Metro 서버 필요)

```bash
npx nx build-android AndroidApp
```

### 빌드 (QA 배포용, JS 번들 내장, Metro 서버 불필요)

```bash
npx nx build-android-qa AndroidApp
```

### 빌드 (Release)

```bash
npx nx build-android-release AndroidApp
```

### 실행 (에뮬레이터/디바이스 필요)

```bash
npx nx run-android AndroidApp
```

### 클린 빌드

```bash
npx nx clean AndroidApp
```

---

## React Native 앱 (RnApp)

### Metro 서버 시작

```bash
npx nx start RnApp
```

### Android에서 RN 앱 실행

```bash
npx nx run-android RnApp
```

### iOS에서 RN 앱 실행

```bash
npx nx run-ios RnApp
```

---

## 유틸리티

### 프로젝트 목록 확인

```bash
npx nx show projects
```

### 의존성 그래프 보기

```bash
npx nx graph
```
