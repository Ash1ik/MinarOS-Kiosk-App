package com.example.demoapp.menu

import android.content.Context
import android.view.KeyEvent
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.ui.theme.BrandColor
import androidx.core.content.edit
import com.example.demoapp.TvButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBackPressed: () -> Unit) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("MinarosPrefs", Context.MODE_PRIVATE) }

    // Track both Base URL and Endpoint dynamically
    var currentBaseUrl by remember {
        mutableStateOf(sharedPrefs.getString("BASE_URL", "https://minaros.com/") ?: "https://minaros.com/")
    }

    // Only track and save the Endpoint now
    var currentEndpoint by remember {
        mutableStateOf(sharedPrefs.getString("TARGET_ENDPOINT", "100001") ?: "100001")
    }

    var isCacheEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("ENABLE_CACHE", true)) }
    var updateStatus by remember { mutableStateOf("App is up to date (v1.0)") }
    val scope = rememberCoroutineScope()
    val backButtonFocus = remember { FocusRequester() }

    LaunchedEffect(Unit) { backButtonFocus.requestFocus() }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // --- HEADER --- (Keep your existing Header code here)
        Row(
            modifier = Modifier.fillMaxWidth().background(BrandColor).padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var isBackFocused by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .focusRequester(backButtonFocus)
                    .onFocusChanged { isBackFocused = it.isFocused }
                    .focusable()
                    .clickable { onBackPressed() }
                    .background(if (isBackFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Kiosk Settings", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }

        // --- CONTENT ---
        Column(modifier = Modifier.padding(48.dp)) {

            // 1. Base URL
            Text("Base URL", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BrandColor)
            OutlinedTextField(
                value = currentBaseUrl,
                onValueChange = { currentBaseUrl = it }, // Does nothing, it's read-only
                enabled = true, // Disables focus and typing
                singleLine = true,
                modifier = Modifier.fillMaxWidth(0.6f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Mosque ID / Endpoint (Editable)
            Text("Mosque ID", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = currentEndpoint,
                    onValueChange = { currentEndpoint = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                TvButton("Save Configuration") {
                    // Save ONLY the endpoint to SharedPreferences
                    sharedPrefs.edit { putString("TARGET_ENDPOINT", currentEndpoint) }
                    Toast.makeText(context, "Saved! Please refresh the main screen.", Toast.LENGTH_LONG).show()
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(32.dp))

            // Cache Setting
            Text("Web Caching", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (isCacheEnabled) "Web Caching is ENABLED" else "Web Caching is DISABLED", modifier = Modifier.weight(1f))
                Switch(
                    checked = isCacheEnabled,
                    onCheckedChange = {
                        isCacheEnabled = it
                        sharedPrefs.edit { putBoolean("ENABLE_CACHE", it) }
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(modifier = Modifier.height(32.dp))

            // Updates
            Text("System Updates", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TvButton("Check for Updates") {
                    scope.launch {
                        updateStatus = "Checking remote server..."
                        delay(1500)
                        updateStatus = "App is up to date (v1.0)"
                        Toast.makeText(context, updateStatus, Toast.LENGTH_SHORT).show()
                    }
                }
                Spacer(modifier = Modifier.width(24.dp))
                Text(updateStatus, color = Color.DarkGray, fontSize = 16.sp)
            }
        }
    }
}