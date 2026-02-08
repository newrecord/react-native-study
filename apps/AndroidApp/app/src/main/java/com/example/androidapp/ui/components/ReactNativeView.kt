package com.example.androidapp.ui.components

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
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
 * SurfaceHolder를 통해 Surface를 캐싱하여 탭 전환 시 JS 상태를 유지한다.
 * - 탭 전환(Composable dispose): View를 부모에서 분리만 하고 Surface는 유지
 * - 탭 복귀(Composable recompose): 캐싱된 Surface의 View를 재사용
 * - Activity destroy: SurfaceHolder.clear()로 전체 정리
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
        SurfaceHolder.getOrCreate(reactHost, context, moduleName, initialProperties)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME ->
                    reactHost.onHostResume(context as Activity)
                Lifecycle.Event.ON_PAUSE ->
                    reactHost.onHostPause(context as Activity)
                Lifecycle.Event.ON_DESTROY -> {
                    reactHost.onHostDestroy(context as Activity)
                    SurfaceHolder.clear()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            // Surface는 stop하지 않음 — JS 상태 유지를 위해 캐시에 보관
            // View만 부모에서 분리하여 재사용 시 "already has a parent" 에러 방지
            (surface.view?.parent as? ViewGroup)?.removeView(surface.view)
        }
    }

    AndroidView(
        factory = { surface.view!! },
        modifier = modifier,
    )
}
