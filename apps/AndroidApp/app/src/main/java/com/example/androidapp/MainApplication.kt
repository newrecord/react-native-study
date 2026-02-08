package com.example.androidapp

import android.app.Application
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultReactHost.getDefaultReactHost
import com.facebook.react.shell.MainReactPackage
import com.facebook.soloader.SoLoader
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application(), ReactApplication {

    /**
     * ReactHost 싱글톤 인스턴스 (New Architecture).
     *
     * ReactHost는 Bridge의 ReactNativeHost/ReactInstanceManager를 대체:
     * - JSI(JavaScript Interface): JS ↔ Native 직접 호출 (JSON 직렬화 없음)
     * - Fabric 렌더러: 네이티브 UI 트리를 C++에서 직접 관리
     * - TurboModules: 네이티브 모듈을 필요 시점에 지연 로딩
     *
     * lazy 초기화: RN 화면 최초 진입 시에만 Hermes 엔진이 로드됨.
     */
    val reactHostInstance: ReactHost by lazy {
        getDefaultReactHost(
            context = applicationContext,
            packageList = reactPackages,
        )
    }

    /**
     * 등록할 네이티브 모듈 패키지 목록.
     * MainReactPackage: RN 코어 모듈 (Text, View, Image 등)
     * Task 5에서 커스텀 NativeModule/TurboModule 패키지를 여기에 추가.
     */
    private val reactPackages: List<ReactPackage>
        get() = listOf(MainReactPackage())

    /**
     * ReactApplication 인터페이스 구현.
     * RN 내부(dev menu, error overlay 등)가 이 property로 ReactHost를 찾음.
     */
    override val reactHost: ReactHost
        get() = reactHostInstance

    override fun onCreate() {
        super.onCreate()
        // SoLoader: RN의 네이티브 라이브러리(.so) 로더
        // Hermes, JSI, Yoga, Fabric 등의 .so 파일을 로드
        // 반드시 ReactHost 접근 전에 호출해야 함
        SoLoader.init(this, false)
    }
}
