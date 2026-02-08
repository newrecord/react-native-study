package com.example.androidapp

import android.app.Application
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.react.soloader.OpenSourceMergedSoMapping
import com.facebook.soloader.SoLoader
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt + React Native New Architecture 통합.
 *
 * ReactApplication 인터페이스를 구현하여 RN 런타임을 관리한다.
 * - reactNativeHost: 패키지 목록, JS 엔트리, 개발 모드 등 설정
 * - reactHost: New Architecture(Fabric + TurboModules) 런타임
 * - SoLoader: 네이티브 라이브러리 로딩 (OpenSourceMergedSoMapping으로 .so 매핑)
 * - DefaultNewArchitectureEntryPoint.load(): New Architecture 네이티브 코드 초기화
 */
@HiltAndroidApp
class MainApplication : Application(), ReactApplication {

    override val reactNativeHost: ReactNativeHost =
        object : DefaultReactNativeHost(this) {
            override fun getPackages(): List<ReactPackage> =
                PackageList(this).packages

            override fun getJSMainModuleName(): String = "index"

            override fun getUseDeveloperSupport(): Boolean = BuildConfig.DEBUG

            override val isNewArchEnabled: Boolean = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
            override val isHermesEnabled: Boolean = BuildConfig.IS_HERMES_ENABLED
        }

    override val reactHost: ReactHost
        get() = getDefaultReactHost(applicationContext, reactNativeHost)

    override fun onCreate() {
        super.onCreate()
        SoLoader.init(this, OpenSourceMergedSoMapping)
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            load()
        }
    }
}
