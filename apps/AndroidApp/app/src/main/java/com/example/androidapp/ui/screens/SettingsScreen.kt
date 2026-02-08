package com.example.androidapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidapp.ui.components.ReactNativeView
import com.facebook.react.ReactHost

@Composable
fun SettingsScreen(reactHost: ReactHost) {
    ReactNativeView(
        reactHost = reactHost,
        moduleName = "SettingsModule",
        modifier = Modifier.fillMaxSize(),
    )
}
