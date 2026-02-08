package com.example.androidapp.ui.screens

import android.os.Bundle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.androidapp.ui.components.ReactNativeView
import com.facebook.react.ReactHost

@Composable
fun SettingsScreen(reactHost: ReactHost) {
    val initialProps = remember {
        Bundle().apply {
            putString("userName", "홍길동")
            putString("appVersion", "1.0.0")
            putString("themeName", "light")
        }
    }

    ReactNativeView(
        reactHost = reactHost,
        moduleName = "SettingsModule",
        modifier = Modifier.fillMaxSize(),
        initialProperties = initialProps,
    )
}
