package com.example.demoapp.menu.settings.sections

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.demoapp.ui.theme.BrandColor

@OptIn(ExperimentalTvMaterial3Api::class)
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
                // 🎯 FIX 1: Intercept the hardware D-pad click directly for instant 1-click expansion
                .onKeyEvent { keyEvent ->
                    if (isHeaderFocused &&
                        keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                        (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        isExpanded = !isExpanded
                        return@onKeyEvent true // Event consumed successfully
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
                            // 🎯 FIX 2: Intercept hardware selection inside the sub-list for single-click confirmation
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showConfirmationDialog = false }
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            sharedPrefs.edit { putInt("SAVED_ORIENTATION", pendingRotationFlag) }
                            showConfirmationDialog = false
                            Toast.makeText(context, "Saved successfully! Changes will apply on refresh or restart.", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandColor)
                    ) {
                        Text("Save", color = Color.White)
                    }
                }
            }
        )
    }
}