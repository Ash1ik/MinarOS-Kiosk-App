package com.example.minaros.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.minaros.ui.screens.dashboard.MinarOsAppScreen
import com.example.minaros.ui.screens.about.AboutScreen
import com.example.minaros.ui.screens.settings.SettingsScreen

/**
 * Centralized constant references for Jetpack Compose navigation routing strings.
 */
object MenuScreen {
    const val SETTINGS_SCREEN = "settings_screen"
    const val ABOUT_SCREEN = "about_screen"
}

/**
 * The core router for the authenticated portion of the application.
 * Manages the transitions between the main dashboard display and the various settings menus.
 */
@Composable
fun MinarOSNavGraph(
    currentOrientation: Int,
    onOrientationChange: (Int) -> Unit,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    val navController = rememberNavController()
    val mainScreen = "main_screen"

    NavHost(navController = navController, startDestination = mainScreen) {

        composable(mainScreen) {
            MinarOsAppScreen(
                navController = navController,
                onOrientationChange = onOrientationChange,
                currentOrientation = currentOrientation
            )
        }

        composable(MenuScreen.SETTINGS_SCREEN) {
            SettingsScreen(
                onBackPressed = { navController.popBackStack() },
                onOrientationChange = onOrientationChange,
                onAlwaysOnChanged = onAlwaysOnChanged
            )
        }

        composable(MenuScreen.ABOUT_SCREEN) {
            AboutScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}