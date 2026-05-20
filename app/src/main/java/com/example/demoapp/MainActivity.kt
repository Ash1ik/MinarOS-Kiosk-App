package com.example.demoapp

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.core.content.edit
import java.io.DataOutputStream

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
        android.webkit.WebView.setWebContentsDebuggingEnabled(true)

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
                        fadeOut(
                            animationSpec = tween(
                                500
                            )
                        )
                    )
                },
                label = "AppLaunchTransition"
            ) { state ->
                when (state) {
                    AppLaunchState.SPLASH -> {
                        CustomSplashScreen(
                            onSplashFinished = {
                                // Splash timer ended. Look up the persistent ID to route the user safely.
                                val savedMosqueId = sharedPrefs.getString("MOSQUE_ID", null)

                                currentLaunchState = if (savedMosqueId.isNullOrEmpty()) {
                                    AppLaunchState.WELCOME
                                } else {
                                    AppLaunchState.MAIN_DISPLAY
                                }
                            }
                        )
                    }

                    AppLaunchState.WELCOME -> {
                        // FIXED: Removed the redundant 'isAppConfigured' conditional wrap.
                        // The state machine only visits this branch if the Mosque ID is genuinely missing.
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
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                internalFlipAngle = 180f
            }

            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                internalFlipAngle = 0f
            }

            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> {
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                internalFlipAngle = 180f
            }
        }
    }

}