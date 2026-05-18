package com.example.demoapp

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.demoapp.menu.settings.SettingsScreen


object MenuScreen {
    const val SETTINGS_SCREEN = "settings_screen"
}

@Composable
fun MinarOSNavGraph(
    currentOrientation: Int,
    onOrientationChange: (Int) -> Unit,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val mainScreen = "main_screen"

    NavHost(navController = navController, startDestination =  mainScreen) {

        // 1. The Main Web View & Drawer Screen
        composable(mainScreen) {
            MinarOsAppScreen(
                navController = navController,
                onOrientationChange = onOrientationChange,
                currentOrientation = currentOrientation
            )
        }

        // 2. The Dedicated Settings Screen
        composable(MenuScreen.SETTINGS_SCREEN) {
            SettingsScreen(
                onBackPressed = {
                    navController.popBackStack()
                },
                onOrientationChange = onOrientationChange,
                onAlwaysOnChanged = onAlwaysOnChanged
            )
        }
    }
}