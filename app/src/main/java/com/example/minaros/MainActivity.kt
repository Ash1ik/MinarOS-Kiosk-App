package com.example.minaros

import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.WindowManager
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.minaros.data.MosqueDataManager
import com.example.minaros.ui.screens.dashboard.AppNavigationWrapper
import com.example.minaros.ui.screens.splash.CustomSplashScreen
import com.example.minaros.ui.screens.splash.WelcomeScreen

/**
 * Defines the top-level routing states for the application.
 */
enum class AppLaunchState {
    SPLASH,
    WELCOME,
    MAIN_DISPLAY
}

/**
 * The primary entry point for the MinarOS application.
 * Handles top-level window configuration (fullscreen, keep-screen-on), orientation locking,
 * and Jetpack Compose navigation routing.
 */
class MainActivity : ComponentActivity() {

    // Tracks the current requested screen orientation (e.g., SCREEN_ORIENTATION_PORTRAIT)
    private var currentVisualOrientation by mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    // Tracks the rotation angle needed to visually flip the UI (for reverse orientations)
    private var internalFlipAngle by mutableFloatStateOf(0f)

    @OptIn(ExperimentalTvMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindowConfigurations()
        setupWebViewDebugging()

        val sharedPrefs = getSharedPreferences("MinarOSPrefs", MODE_PRIVATE)
        val savedOrientation = sharedPrefs.getInt("SAVED_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        applySystemAndVisualOrientation(savedOrientation)

        // Keep the screen awake if the user preference allows it (Default: True)
        if (sharedPrefs.getBoolean("ALWAYS_ON_MODE", true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            var currentLaunchState by remember { mutableStateOf(AppLaunchState.SPLASH) }

            AnimatedContent(
                targetState = currentLaunchState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)).togetherWith(
                        fadeOut(animationSpec = tween(500))
                    )
                },
                label = "AppLaunchTransition"
            ) { state ->
                when (state) {
                    AppLaunchState.SPLASH -> {
                        CustomSplashScreen(
                            internalFlipAngle = internalFlipAngle,
                            onSplashFinished = {
                                val savedMosqueId = MosqueDataManager.getMosqueId(this@MainActivity)
                                currentLaunchState = if (savedMosqueId.isEmpty()) {
                                    AppLaunchState.WELCOME
                                } else {
                                    AppLaunchState.MAIN_DISPLAY
                                }
                            }
                        )
                    }

                    AppLaunchState.WELCOME -> {
                        WelcomeScreen(
                            onIdSuccessfullySaved = {
                                currentLaunchState = AppLaunchState.MAIN_DISPLAY
                            }
                        )
                    }

                    AppLaunchState.MAIN_DISPLAY -> {
                        AppNavigationWrapper(
                            currentOrientation = currentVisualOrientation,
                            internalFlipAngle = internalFlipAngle,
                            onOrientationChange = { orientation ->
                                applySystemAndVisualOrientation(orientation)
                                sharedPrefs.edit { putInt("SAVED_ORIENTATION", orientation) }
                            },
                            onAlwaysOnChanged = { isEnabled ->
                                if (isEnabled) {
                                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                } else {
                                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Configures the window to hide system UI bars (status bar and navigation bar)
     * and enables swipe-to-reveal behavior.
     */
    private fun setupWindowConfigurations() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    /**
     * Safely enables WebView debugging if the application is built in debug mode.
     * Uses a try-catch to prevent crashes on custom TV hardware lacking the standard WebView package.
     */
    private fun setupWebViewDebugging() {
        try {
            val isDebuggable = (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE))
            if (isDebuggable) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Applies the requested orientation to the system window and updates the internal
     * flip angle state used for Compose rotation modifiers.
     *
     * @param orientation The requested ActivityInfo screen orientation constant.
     */
    private fun applySystemAndVisualOrientation(orientation: Int) {
        currentVisualOrientation = orientation
        when (orientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                internalFlipAngle = 0f
            }
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                internalFlipAngle = 180f
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                internalFlipAngle = 0f
            }
            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                internalFlipAngle = 180f
            }
        }
    }
}