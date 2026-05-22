package com.example.demoapp

import android.content.pm.ActivityInfo
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

// Clean, isolated routing definitions
enum class AppLaunchState {
    SPLASH,
    WELCOME,
    MAIN_DISPLAY
}

class MainActivity : ComponentActivity() {

    private var currentVisualOrientation by mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    private var internalFlipAngle by mutableFloatStateOf(0f)

    @OptIn(ExperimentalTvMaterial3Api::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        WindowInsetsControllerCompat(window, window.decorView).apply {
            // Hides both the navigation bar (bottom) and status bar (top)
            hide(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.statusBars())

            // 🎯 BEHAVIOR STYLE: Makes the bars reappear temporarily as overlays if the user swipes
            // from the screen edges, then automatically hides them again after a few seconds.
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // 🎯 CRITICAL FIX: Safe debugging setup wrapper.
        // Prevents InvocationTargetException when com.google.android.webview is missing on custom TV hardware.
        try {
            // Falls back to checking the application context flags instead of a generated class
            val isDebuggable = (0 != (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE))
            if (isDebuggable) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Using "MinarOSPrefs" to stay consistent with your preference keys across files
        val sharedPrefs = getSharedPreferences("MinarOSPrefs", MODE_PRIVATE)
        val savedOrientation =
            sharedPrefs.getInt("SAVED_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

        applySystemAndVisualOrientation(savedOrientation)

        if (sharedPrefs.getBoolean("ALWAYS_ON_MODE", true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            // App always mounts strictly on the SPLASH branch first
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
                            internalFlipAngle = internalFlipAngle, // 🎯 FIX 3: Push the live angle calculation context downstream
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
                                // 1. Update your visual tracker tracking properties
                                applySystemAndVisualOrientation(orientation)

                                // 2. Write configuration parameters to SharedPreferences database maps
                                sharedPrefs.edit { putInt("SAVED_ORIENTATION", orientation) }
                            },
                            onAlwaysOnChanged = { isEnabled ->
                                if (isEnabled) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                                else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                            }
                        )
                    }
                }
            }
        }
    }

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