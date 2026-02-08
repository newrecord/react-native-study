package com.example.androidapp.bridge

import android.widget.Toast
import com.example.androidapp.BuildConfig
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Native <-> RN 양방향 통신을 위한 Bridge 모듈.
 *
 * RN 측에서 NativeModules.AppBridge로 접근한다.
 * - showToast: 네이티브 Toast 표시
 * - navigateToNativeScreen: Compose Navigation으로 화면 전환
 * - getAppInfo: Promise로 앱 정보 반환
 * - requestThemeChange: 테마 변경 요청 → 네이티브 처리 후 이벤트로 결과 전송
 */
class AppBridgeModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String = MODULE_NAME

    /**
     * RN → Native: Toast 메시지 표시
     */
    @ReactMethod
    fun showToast(message: String) {
        currentActivity?.runOnUiThread {
            Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * RN → Native: Compose Navigation 화면 전환 요청.
     * SharedFlow를 통해 AppNavigation Composable에 전달된다.
     */
    @ReactMethod
    fun navigateToNativeScreen(screenName: String) {
        _navigationEvents.tryEmit(screenName)
    }

    /**
     * RN → Native: Promise 기반 앱 정보 조회
     */
    @ReactMethod
    fun getAppInfo(promise: Promise) {
        val info = Arguments.createMap().apply {
            putString("appName", "AndroidApp")
            putString("version", "1.0.0")
            putString("buildType", if (BuildConfig.DEBUG) "debug" else "release")
            putBoolean("newArchEnabled", BuildConfig.IS_NEW_ARCHITECTURE_ENABLED)
        }
        promise.resolve(info)
    }

    /**
     * RN → Native → RN: 테마 변경 요청 (양방향 통신 데모).
     * 네이티브에서 처리 후 DeviceEventEmitter로 결과를 RN에 전송한다.
     */
    @ReactMethod
    fun requestThemeChange(themeName: String) {
        val params = Arguments.createMap().apply {
            putString("theme", themeName)
            putDouble("timestamp", System.currentTimeMillis().toDouble())
        }
        sendEvent("onThemeChanged", params)
    }

    /**
     * Native → RN: DeviceEventEmitter를 통한 이벤트 전송.
     * 네이티브 코드 어디서든 이 메서드를 호출하여 RN에 이벤트를 보낼 수 있다.
     */
    fun sendEvent(eventName: String, params: WritableMap?) {
        reactApplicationContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    // RN의 NativeEventEmitter 구독/해제 시 경고 방지용
    @ReactMethod
    fun addListener(eventName: String) {}

    @ReactMethod
    fun removeListeners(count: Int) {}

    companion object {
        const val MODULE_NAME = "AppBridge"

        private val _navigationEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
        val navigationEvents = _navigationEvents.asSharedFlow()
    }
}
