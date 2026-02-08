package com.example.androidapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.androidapp.bridge.AppBridgeModule
import com.example.androidapp.ui.components.BottomNavBar
import com.example.androidapp.ui.components.NavItem
import com.example.androidapp.ui.screens.ChatScreen
import com.example.androidapp.ui.screens.HistoryScreen
import com.example.androidapp.ui.screens.HomeScreen
import com.example.androidapp.ui.screens.SearchScreen
import com.example.androidapp.ui.screens.SettingsScreen
import com.facebook.react.ReactHost

@Composable
fun AppNavigation(reactHost: ReactHost) {
    val navController = rememberNavController()
    var selectedItem by remember { mutableStateOf(NavItem.HOME) }

    // RN에서 navigateToNativeScreen() 호출 시 Compose Navigation으로 전달
    LaunchedEffect(Unit) {
        AppBridgeModule.navigationEvents.collect { screenName ->
            val navItem = NavItem.entries.find { it.route == screenName }
            navItem?.let {
                selectedItem = it
                navController.navigate(it.route) {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                selectedItem = selectedItem,
                onItemSelected = { item ->
                    selectedItem = item
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavItem.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavItem.HOME.route) { HomeScreen() }
            composable(NavItem.HISTORY.route) { HistoryScreen() }
            composable(NavItem.SEARCH.route) { SearchScreen() }
            composable(NavItem.CHAT.route) { ChatScreen() }
            composable(NavItem.SETTINGS.route) { SettingsScreen(reactHost) }
        }
    }
}
