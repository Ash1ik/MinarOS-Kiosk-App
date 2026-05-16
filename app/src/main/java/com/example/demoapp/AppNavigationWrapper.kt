package com.example.demoapp

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.ui.theme.BrandColor
import kotlinx.coroutines.delay

@Composable
fun AppNavigationWrapper() {
    // State to track if the splash screen is showing
    var showSplash by remember { mutableStateOf(true) }

    // Timer for the Splash Screen
    LaunchedEffect(key1 = true) {
        delay(2500L) // Show splash for 2.5 seconds
        showSplash = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. The Main App (Loads in the background)
        // Only render the WebView/App if the splash is done, or let it load behind?
        // Usually best to let it mount behind the splash screen so the WebView has time to load.
        MinarOSNavGraph(
            onOrientationChange = {}
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

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "MINAROS App",
            color = BrandColor ,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Version: 1.0",
            color = BrandColor.copy(alpha = 0.75f),
            fontSize = 18.sp
        )
    }
}