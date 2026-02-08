package com.example.androidapp.di

import android.app.Application
import com.example.androidapp.MainApplication
import com.facebook.react.ReactHost
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * ReactHost를 Hilt 싱글톤으로 제공 (New Architecture).
     *
     * ReactHost는 다음을 관리:
     * - Hermes JS 엔진 생명주기
     * - Fabric 렌더러 (네이티브 UI 직접 생성)
     * - TurboModules (네이티브 모듈 지연 로딩)
     *
     * MainApplication에서 생성한 인스턴스를 그대로 반환하여
     * ReactApplication 인터페이스와 Hilt DI가 동일한 인스턴스를 공유.
     *
     * Task 4에서 Compose AndroidView 내에서 ReactSurface를 생성할 때 사용.
     */
    @Provides
    @Singleton
    fun provideReactHost(application: Application): ReactHost =
        (application as MainApplication).reactHostInstance
}
