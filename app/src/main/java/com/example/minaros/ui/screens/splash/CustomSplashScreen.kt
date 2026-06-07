package com.example.minaros.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.example.minaros.screen.R

/**
 * A custom, animated splash screen displayed during application cold boots.
 * Enforces the same visual orientation rules as the main app wrapper.
 *
 * @param internalFlipAngle The calculated degrees to rotate the layout (e.g., 0f or 180f).
 * @param onSplashFinished Callback triggered after the minimum display duration completes.
 */
@Composable
fun CustomSplashScreen(
    internalFlipAngle: Float,
    onSplashFinished: () -> Unit
) {
    // Triggers the background timer safely tied to the Composable lifecycle
    LaunchedEffect(key1 = true) {
        delay(2500L) // 2.5 second display duration
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .graphicsLayer {
                // Ensures the splash screen matches the TV's mounted orientation instantly
                rotationZ = internalFlipAngle
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_minaros_logo),
                contentDescription = "MinarOs Logo",
                modifier = Modifier.size(160.dp)
            )
        }
    }
}