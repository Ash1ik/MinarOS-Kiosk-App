package com.example.demoapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AppNavigationWrapper(
    onOrientationChange: (Int) -> Unit,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    // State to track if the splash screen is showing
    var showSplash by rememberSaveable { mutableStateOf(true) }

    // Timer for the Splash Screen
    LaunchedEffect(key1 = true) {
        if (showSplash) {
            delay(2500L)
            showSplash = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. The Main App (Loads in the background)
        // Only render the WebView/App if the splash is done, or let it load behind?
        // Usually best to let it mount behind the splash screen so the WebView has time to load.
        MinarOSNavGraph(
            onOrientationChange = onOrientationChange,
            onAlwaysOnChanged = onAlwaysOnChanged
        )

        // 2. The Splash Screen Overlay
        AnimatedVisibility(
            visible = showSplash,
            exit = fadeOut(animationSpec = tween(durationMillis = 800)) // Smooth fade out
        ) {
            CustomSplashScreen()
        }
    }
}

@Composable
fun CustomSplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // The Transparent Mosque Logo
        Image(
            painter = painterResource(id = R.drawable.ic_minaros_logo),
            contentDescription = "MinarOs Logo",
            modifier = Modifier.size(160.dp) // Make it nice and big for the TV
        )
    }
}