package com.example.androidapp.ui.components

import android.app.Activity
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.facebook.react.ReactHost

/**
 * Compose 안에서 React Native 화면을 렌더링하는 재사용 가능한 Composable.
 *
 * New Architecture의 ReactHost + ReactSurface API를 사용한다.
 * ReactHost.createSurface()로 Fabric Surface를 생성하고,
 * surface.start()로 렌더링을 시작한다.
 * ReactHost의 생명주기를 Compose LifecycleOwner와 동기화한다.
 *
 * @param reactHost Hilt로 주입받은 ReactHost 싱글톤
 * @param moduleName RN 측 AppRegistry.registerComponent()에 등록된 모듈 이름
 * @param modifier Compose Modifier
 * @param initialProperties 네이티브에서 RN으로 전달할 초기 데이터 (Bundle)
 */
@Composable
fun ReactNativeView(
    reactHost: ReactHost,
    moduleName: String,
    modifier: Modifier = Modifier,
    initialProperties: Bundle? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val surface = remember(moduleName) {
        reactHost.createSurface(context, moduleName, initialProperties)
    }

    DisposableEffect(lifecycleOwner, surface) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME ->
                    reactHost.onHostResume(context as Activity)
                Lifecycle.Event.ON_PAUSE ->
                    reactHost.onHostPause(context as Activity)
                Lifecycle.Event.ON_DESTROY ->
                    reactHost.onHostDestroy(context as Activity)
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        surface.start()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            surface.stop()
        }
    }

    AndroidView(
        factory = { surface.view!! },
        modifier = modifier,
    )
}
