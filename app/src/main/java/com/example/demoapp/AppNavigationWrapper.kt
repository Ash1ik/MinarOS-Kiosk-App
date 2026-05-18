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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun AppNavigationWrapper(
    currentOrientation: Int,
    internalFlipAngle: Float,
    onOrientationChange: (Int) -> Unit,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(key1 = true) {
        delay(2500L)
        showSplash = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // If the TV requires an upside-down mount adjustment,
                    // this spins the responsive window perfectly into place.
                    rotationZ = internalFlipAngle
                }
        ) {
            MinarOSNavGraph(
                onOrientationChange = onOrientationChange,
                onAlwaysOnChanged = onAlwaysOnChanged,
                currentOrientation = currentOrientation
            )

            AnimatedVisibility(
                visible = showSplash,
                exit = fadeOut(animationSpec = tween(800))
            ) {
                CustomSplashScreen()
            }
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