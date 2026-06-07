package com.example.minaros.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.minaros.ui.navigation.MinarOSNavGraph

/**
 * A structural container that wraps the main application navigation graph.
 * It applies global hardware orientation flips (rotationZ) natively to the entire
 * Compose tree, allowing TV displays mounted upside-down to render correctly.
 *
 * @param currentOrientation The current system orientation flag.
 * @param internalFlipAngle The calculated degrees to rotate the layout (e.g., 0f or 180f).
 * @param onOrientationChange Callback to trigger a system-level rotation.
 * @param onAlwaysOnChanged Callback to toggle the screen-awake wakelock.
 */
@Composable
fun AppNavigationWrapper(
    currentOrientation: Int,
    internalFlipAngle: Float,
    onOrientationChange: (Int) -> Unit,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    // Applies hardware-level inverse rotation to the entire UI tree
                    rotationZ = internalFlipAngle
                }
        ) {
            MinarOSNavGraph(
                onOrientationChange = onOrientationChange,
                onAlwaysOnChanged = onAlwaysOnChanged,
                currentOrientation = currentOrientation
            )
        }
    }
}