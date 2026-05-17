package com.example.demoapp

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.core.content.edit

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.webkit.WebView.setWebContentsDebuggingEnabled(true)

        // 1. Initialize SharedPreferences
        val sharedPrefs = getSharedPreferences("MinarOSPrefs", MODE_PRIVATE)

        // 2. READ AND APPLY ORIENTATION ON BOOT
        // We default to Landscape if nothing has been saved yet
        val savedOrientation = sharedPrefs.getInt("SAVED_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        requestedOrientation = savedOrientation

        // 3. Keep screen wake lock setup
        if (sharedPrefs.getBoolean("ALWAYS_ON_MODE", true)) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        // 4. Immersive Fullscreen Mode
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        setContent {
            // Update your wrapper call to handle BOTH orientation and wake lock changes
            AppNavigationWrapper(
                onOrientationChange = { orientation ->
                    requestedOrientation = orientation
                    sharedPrefs.edit().putInt("SAVED_ORIENTATION", orientation).apply()
                },
                onAlwaysOnChanged = { isEnabled ->
                    // This updates the hardware window flags LIVE without restarting the app!
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
