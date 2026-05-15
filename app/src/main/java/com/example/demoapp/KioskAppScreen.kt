package com.example.demoapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.key.onKeyEvent
import android.view.KeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled", "ContextCastToActivity")
@Composable
fun KioskAppScreen(onOrientationChange: (Int) -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current as? Activity
    val focusManager = LocalFocusManager.current
    val firstItemFocusRequester = remember { FocusRequester() }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val exitThreshold = 500L
    val minDelay = 100L

    val loadUrl = "https://minaros.com/100001"

    // Auto-Select Logic: Triggers when the drawer opens
    // Auto-Select & Memory Wipe Logic
    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Open) {
            // 1. Trigger focus the exact millisecond the drawer STARTS opening.
            // A microscopic 10ms delay ensures the UI tree is built without any visible lag.
            kotlinx.coroutines.delay(10)
            try {
                firstItemFocusRequester.requestFocus()
            } catch (e: Exception) {
                // Failsafe in case the UI tree takes a frame longer to build
            }
        } else if (drawerState.targetValue == DrawerValue.Closed) {
            // 2. When closing: Wait for the slide animation to finish (300ms), then wipe memory.
            kotlinx.coroutines.delay(300)
            focusManager.clearFocus()
        }
    }

    BackHandler(enabled = true) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastClick = currentTime - lastBackPressTime

        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            lastBackPressTime = 0L
            return@BackHandler
        }

        if (timeSinceLastClick in minDelay..exitThreshold) {
            context?.finish()
        } else {
            lastBackPressTime = currentTime
            scope.launch { drawerState.open() }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(280.dp),
                drawerContainerColor = Color.White
            ) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Kiosk Settings",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(16.dp)
                )
                HorizontalDivider(color = Color.LightGray)

                // 1. First Item
                DrawerMenuItem(
                    text = "Set Landscape",
                    modifier = Modifier.focusRequester(firstItemFocusRequester)
                ) {
                    scope.launch {
                        // 1st: Wait for the drawer to fully close
                        drawerState.close()
                        // 2nd: Execute the rotation AFTER it is hidden
                        onOrientationChange(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    }
                }

                // 2. Second Item
                DrawerMenuItem(
                    text = "Set Portrait"
                ) {
                    scope.launch {
                        drawerState.close()
                        onOrientationChange(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    }
                }

                // 3. Third Item
                DrawerMenuItem(
                    text = "Close Menu"
                ) {
                    scope.launch {
                        drawerState.close()
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webViewClient = WebViewClient()
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                        }
                        loadUrl(loadUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


@Composable
fun DrawerMenuItem(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Text(
        text = text,
        color = if (isFocused) Color.Black else Color.Gray,
        fontWeight = if (isFocused) FontWeight.Bold else FontWeight.Normal,
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { isFocused = it.isFocused }
            // .clickable inherently makes it focusable and handles TV D-Pad Enter
            .clickable { onClick() }
            // Failsafe: Explicitly listen for the D-Pad Center/Enter key to trigger click instantly
            .onKeyEvent { keyEvent ->
                if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onClick()
                    true // Tells Android "I handled this click, don't do anything else"
                } else {
                    false
                }
            }
            .background(if (isFocused) Color(0xFFE0E0E0) else Color.Transparent)
            .padding(16.dp)
    )
}

