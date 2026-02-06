package com.example.androidapp.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // 추후 ReactNativeHost 등 의존성 주입 시 사용
}
