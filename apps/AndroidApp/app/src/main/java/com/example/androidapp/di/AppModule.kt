package com.example.androidapp.di

import android.app.Application
import com.facebook.react.ReactApplication
import com.facebook.react.ReactHost
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideReactHost(application: Application): ReactHost =
        (application as ReactApplication).reactHost
            ?: throw IllegalStateException("ReactHost is not initialized")
}
