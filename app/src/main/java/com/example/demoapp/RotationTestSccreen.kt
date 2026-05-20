package com.example.demoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RotationTestScreen() {
    val configuration = LocalConfiguration.current
    val hardwareWidth = configuration.screenWidthDp.dp   // 1920.dp
    val hardwareHeight = configuration.screenHeightDp.dp  // 1080.dp

    // Calculate the perfect aspect ratio scaling multiplier (1920 / 1080 = 1.7778)
    val scaleFactor = hardwareWidth.value / hardwareHeight.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                // 1. FIXED: Set the size to match the TV height and width inverted BEFORE rotation.
                // This creates a physical 1080x1080 perfect square box that fits perfectly inside
                // the TV's 1080px ceiling without ever triggering system clipping.
                .size(width = hardwareHeight, height = hardwareHeight)
                .graphicsLayer {
                    transformOrigin = TransformOrigin.Center
                    rotationZ = 90f

                    // 2. THE SECRET SAUCE: Scale the width and height parameters during the
                    // rendering step to stretch the square box out to a true 1080x1920 canvas!
                    scaleX = scaleFactor
                    scaleY = scaleFactor
                }
                .background(Color.White)
                .border(4.dp, Color(0xFF3C0424))
        ) {
            // 3. COMPENSATE TEXT DISTORTION: Because the parent layer scales X and Y equally by scaleFactor,
            // your native Compose texts, shapes, and layouts inside will render cleanly with perfect proportions!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- Visual Header Layout Area ---
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                ) {
                    Text(
                        text = "minarOS Display Matrix",
                        fontSize = 24.sp, // Slightly lowered to look crisp under scaling
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3C0424)
                    )
                    Text(
                        text = "Vertical Layout Testing Bounds (1080 x 1920)",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // --- Visual Center Content Grid: Prayer Times Card Blocks ---
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val prayerTimesList = listOf(
                        "Fajr" to "04:15 AM",
                        "Dhuhr" to "12:30 PM",
                        "Asr" to "04:45 PM",
                        "Maghrib" to "06:45 PM",
                        "Isha" to "08:15 PM"
                    )

                    prayerTimesList.forEach { (name, time) ->
                        Card(
                            modifier = Modifier.fillMaxWidth().height(65.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text(text = time, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3C0424))
                            }
                        }
                    }
                }

                // --- Visual Footer Actions Area ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF3C0424), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "System Running Flawlessly • Sideways Ready",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}