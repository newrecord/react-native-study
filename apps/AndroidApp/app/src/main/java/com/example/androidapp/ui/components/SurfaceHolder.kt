package com.example.androidapp.ui.components

import android.content.Context
import android.os.Bundle
import com.facebook.react.ReactHost
import com.facebook.react.interfaces.fabric.ReactSurface

/**
 * ReactSurface를 moduleName 기준으로 캐싱하여 탭 전환 시 재사용한다.
 *
 * 문제: Compose Navigation이 탭 전환 시 Composable을 dispose → 재생성하면
 *       ReactSurface가 stop/start되어 JS 상태(스크롤, 입력값 등)가 초기화됨.
 * 해결: Surface를 싱글톤 캐시에 보관하고, Composable dispose 시 stop하지 않음.
 *       Activity destroy 시에만 전체 정리.
 */
object SurfaceHolder {
    private val surfaces = mutableMapOf<String, ReactSurface>()

    fun getOrCreate(
        reactHost: ReactHost,
        context: Context,
        moduleName: String,
        initialProperties: Bundle?,
    ): ReactSurface {
        return surfaces.getOrPut(moduleName) {
            reactHost.createSurface(context, moduleName, initialProperties).also {
                it.start()
            }
        }
    }

    fun clear() {
        surfaces.values.forEach { it.stop() }
        surfaces.clear()
    }
}
