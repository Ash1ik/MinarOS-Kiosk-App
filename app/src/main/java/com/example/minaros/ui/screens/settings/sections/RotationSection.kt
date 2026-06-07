package com.example.minaros.ui.screens.settings.sections

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.minaros.ui.theme.BrandColor

/**
 * A sub-menu that allows the user to manually flip or lock the application's visual orientation.
 * Useful for TVs that have been physically mounted upside down or vertically.
 *
 * @param selectedRotation The current active orientation flag from [ActivityInfo].
 * @param onRotationSelected Callback to apply and save the new orientation state.
 */
@Composable
fun RotationSection(
    selectedRotation: Int,
    onRotationSelected: (Int) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("MinarOSPrefs", Context.MODE_PRIVATE) }

    var isExpanded by remember { mutableStateOf(false) }
    var isHeaderFocused by remember { mutableStateOf(false) }

    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingRotationFlag by remember { mutableIntStateOf(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) }
    var pendingRotationLabel by remember { mutableStateOf("") }

    val configurations = listOf(
        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE to "Landscape (Standard)",
        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT to "Portrait Left (Vertical)",
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT to "Portrait Right (Flipped Vertical)",
        ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE to "Reverse Landscape (Upside Down)"
    )

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isHeaderFocused) Color.LightGray else Color.Transparent,
                    RoundedCornerShape(8.dp)
                )
                .onFocusChanged { isHeaderFocused = it.isFocused }
                .focusable()
                .onKeyEvent { keyEvent ->
                    if (isHeaderFocused &&
                        keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        isExpanded = !isExpanded
                        return@onKeyEvent true
                    }
                    false
                }
                .clickable { isExpanded = !isExpanded }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Display Orientation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
                Text("Set up your physical TV mounting orientation", fontSize = 14.sp, color = Color.Gray)
            }
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = "Expand Rotation Menu",
                tint = BrandColor,
                modifier = Modifier.size(28.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .background(Color(0xFFF1F3F5), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                configurations.forEach { (orientationFlag, orientationLabel) ->
                    var isItemFocused by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isItemFocused) BrandColor.copy(0.25f) else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .onFocusChanged { isItemFocused = it.isFocused }
                            .focusable()
                            .onKeyEvent { keyEvent ->
                                if (isItemFocused &&
                                    keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                    (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                            keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                                    pendingRotationFlag = orientationFlag
                                    pendingRotationLabel = orientationLabel
                                    showConfirmationDialog = true
                                    return@onKeyEvent true
                                }
                                false
                            }
                            .clickable {
                                pendingRotationFlag = orientationFlag
                                pendingRotationLabel = orientationLabel
                                showConfirmationDialog = true
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (selectedRotation == orientationFlag),
                            onClick = null,
                            colors = RadioButtonDefaults.colors(selectedColor = BrandColor)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = orientationLabel,
                            fontSize = 16.sp,
                            fontWeight = if (selectedRotation == orientationFlag) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRotation == orientationFlag) BrandColor else Color.Black
                        )
                    }
                }
            }
        }
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = {
                Text(
                    text = "Confirm Orientation Change",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to change the visual orientation layout to \"$pendingRotationLabel\"? This adjustment will take effect once the application is manually refreshed or restarted.",
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val cancelInteraction = remember { MutableInteractionSource() }
                    val isCancelFocused by cancelInteraction.collectIsFocusedAsState()

                    Button(
                        onClick = { showConfirmationDialog = false },
                        interactionSource = cancelInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCancelFocused) BrandColor else Color.LightGray.copy(alpha = 0.4f),
                            contentColor = if (isCancelFocused) Color.White else Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    val saveInteraction = remember { MutableInteractionSource() }
                    val isSaveFocused by saveInteraction.collectIsFocusedAsState()

                    Button(
                        onClick = {
                            sharedPrefs.edit { putInt("SAVED_ORIENTATION", pendingRotationFlag) }
                            onRotationSelected(pendingRotationFlag) // 🎯 Update the upstream parent state!
                            showConfirmationDialog = false
                        },
                        interactionSource = saveInteraction,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaveFocused) BrandColor else Color.LightGray.copy(alpha = 0.4f),
                            contentColor = if (isSaveFocused) Color.White else Color.Gray
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        )
    }
}