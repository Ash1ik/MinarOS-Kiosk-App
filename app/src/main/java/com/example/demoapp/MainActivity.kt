package com.example.demoapp

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit

class MainActivity : ComponentActivity() {

    // 1. CRITICAL: Track the visual orientation state explicitly as a state variable
    // This allows you to pass it down to Compose so your rotation toggle loops work seamlessly!
    private var currentVisualOrientation by mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    private var internalFlipAngle by mutableFloatStateOf(0f)

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.webkit.WebView.setWebContentsDebuggingEnabled(true)

        val sharedPrefs = getSharedPreferences("MinarOSPrefs", MODE_PRIVATE)
        val savedOrientation = sharedPrefs.getInt("SAVED_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

        // Setup orientation on boot
        applySystemAndVisualOrientation(savedOrientation)

        if (sharedPrefs.getBoolean("ALWAYS_ON_MODE", true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        setContent {
            AppNavigationWrapper(
                currentOrientation = currentVisualOrientation, // 2. Pass the active tracking target state
                internalFlipAngle = internalFlipAngle,
                onOrientationChange = { orientation ->
                    applySystemAndVisualOrientation(orientation)
                    sharedPrefs.edit { putInt("SAVED_ORIENTATION", orientation) }
                },
                onAlwaysOnChanged = { isEnabled ->
                    if (isEnabled) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            )
        }
    }

    private fun applySystemAndVisualOrientation(orientation: Int) {
        // Update the visual tracking state immediately
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