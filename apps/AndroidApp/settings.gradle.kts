pluginManagement {
    includeBuild("../../node_modules/@react-native/gradle-plugin")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.facebook.react.settings")
}

// RN Gradle Plugin의 settings-plugin이 autolinking.json을 생성
// 모노레포 구조에 맞게 workingDirectory를 RnApp으로 지정
extensions.configure<com.facebook.react.ReactSettingsExtension> {
    autolinkLibrariesFromCommand(
        workingDirectory = file("../RnApp"),
        lockFiles = files(
            "../RnApp/package.json",
            "../../package-lock.json"
        )
    )
}

dependencyResolutionManagement {
    // PREFER_PROJECT: RN Gradle Plugin이 로컬 Maven repo를 프로젝트 레벨에서 추가
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AndroidApp"
include(":app")
