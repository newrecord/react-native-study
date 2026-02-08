plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
    id("com.facebook.react")
}

react {
    // 모노레포 경로 설정: app/ 기준 상대경로
    root = file("../../RnApp")
    reactNativeDir = file("../../../node_modules/react-native")
    codegenDir = file("../../../node_modules/@react-native/codegen")
    cliFile = file("../../../node_modules/@react-native-community/cli/build/bin.js")

    // 모노레포에서 Hermes 컴파일러 경로를 자동 해석하지 못하므로 명시적으로 지정
    val osDir = if (System.getProperty("os.name").lowercase().contains("mac")) "osx-bin" else "linux64-bin"
    hermesCommand.set(file("../../../node_modules/react-native/sdks/hermesc/$osDir/hermesc").absolutePath)

    // debuggableVariants에 포함된 variant는 JS 번들을 APK에 내장하지 않음 (Metro 서버 사용)
    // -PbundleInDebug 전달 시 debug를 제외하여 JS 번들을 APK에 내장 (QA 배포용)
    if (project.hasProperty("bundleInDebug")) {
        debuggableVariants.set(emptyList())
    }
}

android {
    namespace = "com.example.androidapp"
    compileSdk = 36
    ndkVersion = "27.1.12297006"

    defaultConfig {
        applicationId = "com.example.androidapp"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }
    }

    signingConfigs {
        getByName("debug") {
            // 기본 debug keystore 사용
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // 학습용: debug keystore로 Release 서명 (프로덕션에서는 별도 keystore 사용)
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.navigation.compose)

    // React Native - 브라운필드 통합
    implementation(libs.react.android)
    implementation(libs.hermes.android)

    debugImplementation(libs.androidx.ui.tooling)
}
