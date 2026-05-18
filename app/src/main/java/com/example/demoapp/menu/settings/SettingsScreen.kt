package com.example.demoapp.menu.settings

import android.content.Context
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.demoapp.TvButton
import com.example.demoapp.menu.settings.sections.ConstraintsSection
import com.example.demoapp.menu.settings.sections.RotationSection
import com.example.demoapp.menu.settings.sections.StorageSection
import com.example.demoapp.ui.theme.BrandColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onOrientationChange: (Int) -> Unit,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("MinarOSPrefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val backButtonFocus = remember { FocusRequester() }

    // State bindings
    var currentEndpoint by remember {
        mutableStateOf(sharedPrefs.getString("TARGET_ENDPOINT", "100001") ?: "100001")
    }
    var isCacheEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("ENABLE_CACHE", true)) }
    var updateStatus by remember { mutableStateOf("App is up to date (v1.0)") }
    var selectedRotation by remember {
        mutableIntStateOf(
            sharedPrefs.getInt(
                "SAVED_ORIENTATION",
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            )
        )
    }
    var isAlwaysOnEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("ALWAYS_ON_MODE", true))
    }

    LaunchedEffect(Unit) { backButtonFocus.requestFocus() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF8F9FA))
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrandColor)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isBackFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .focusRequester(backButtonFocus)
                    .onFocusChanged { isBackFocused = it.isFocused }
                    .focusable()
                    .clickable { onBackPressed() }
                    .background(
                        if (isBackFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                "MinarOS Settings",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // --- CONTENT HOUSING ---
        Column(modifier = Modifier.padding(24.dp)) {

            // 1. Mosque ID Selection
            Text("Mosque ID", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = currentEndpoint,
                onValueChange = { currentEndpoint = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            TvButton(
                text = "Save Configuration",
                onClick = {
                    sharedPrefs.edit { putString("TARGET_ENDPOINT", currentEndpoint) }
                }
            )

            SectionDivider()

            // 2. Web Caching Setting
            Text("Web Caching", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isCacheEnabled) "Web Caching is ENABLED" else "Web Caching is DISABLED",
                    modifier = Modifier.weight(1f),
                    color = Color.DarkGray
                )
                Switch(
                    checked = isCacheEnabled,
                    onCheckedChange = {
                        isCacheEnabled = it
                        sharedPrefs.edit { putBoolean("ENABLE_CACHE", it) }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BrandColor,
                        checkedTrackColor = BrandColor.copy(alpha = 0.4f)
                    )
                )
            }

            SectionDivider()

            // 3. Screen Rotation Section (Isolated File)
            RotationSection(
                selectedRotation = selectedRotation,
                onRotationSelected = {
                    selectedRotation = it
                    onOrientationChange(selectedRotation)
                },
            )

            SectionDivider()

            // 4. Clear Cache Memory Section (Isolated File)
            StorageSection()

            SectionDivider()

            // 5. App Always-On Mode Section (Isolated File)
            ConstraintsSection(
                isAlwaysOnEnabled = isAlwaysOnEnabled,
                onAlwaysOnChanged = {
                    isAlwaysOnEnabled = it
                    onAlwaysOnChanged(it)
                }
            )

            SectionDivider()

            // 6. System Updates
            Text(
                "System Updates",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BrandColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TvButton(
                    text = "Check for Updates",
                    onClick = {
                        scope.launch {
                            updateStatus = "Checking remote server..."
                            delay(1500)
                            updateStatus = "App is up to date (v1.0)"
                        }
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(updateStatus, color = Color.DarkGray, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SectionDivider() {
    Column {
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.6f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))
    }
}