package com.example.demoapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.demoapp.ui.theme.BrandColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled", "ContextCastToActivity")
@Composable
fun MinarOsAppScreen(
    navController: NavController,
    onOrientationChange: (Int) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current as? Activity
    val focusManager = LocalFocusManager.current
    val sharedPrefs = remember { context?.getSharedPreferences("MinarosPrefs", Context.MODE_PRIVATE) }

    val firstItemFocusRequester = remember { FocusRequester() }

    // Read the endpoint from settings
    val savedEndpoint by remember {
        mutableStateOf(sharedPrefs?.getString("TARGET_ENDPOINT", "100001") ?: "100001")
    }

    // Construct the final, complete URL dynamically
    val fullTargetUrl = "https://minaros.com/$savedEndpoint"

    // Create the WebView with the combined URL
    val webView = remember {
        WebView(context!!).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()

            // Load the combined URL!
            loadUrl(fullTargetUrl)
        }
    }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val exitThreshold = 500L
    val minDelay = 100L

    // Drawer Auto-Focus Logic
    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Open) {
            delay(10)
            try { firstItemFocusRequester.requestFocus() } catch (e: Exception) { }
        } else if (drawerState.targetValue == DrawerValue.Closed) {
            delay(300)
            focusManager.clearFocus()
        }
    }

    // --- UNIFIED BACK HANDLER ---
    BackHandler(enabled = true) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastClick = currentTime - lastBackPressTime

        // Priority 1: If Drawer is open, close it.
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            lastBackPressTime = 0L
            return@BackHandler
        }

        // Priority 2: If the website can go back, go back in history.
        if (webView.canGoBack()) {
            webView.goBack()
            lastBackPressTime = 0L
            return@BackHandler
        }

        // Priority 3: Double click to exit, Single click to open Drawer
        if (timeSinceLastClick in minDelay..exitThreshold) {
            context?.finishAffinity()
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
                modifier = Modifier.width(360.dp),
                drawerContainerColor = Color.White,
                drawerShape = RectangleShape
            ) {
                // --- HEADER SECTION ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandColor)
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_minaros_logo_white),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(120.dp)
                            .padding(start = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
//                    Text("Version: 1.0", color = Color.LightGray, fontSize = 14.sp)
                }

                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                // --- DRAWER ITEMS ---
                Column(modifier = Modifier.fillMaxSize()) {
                    // 1. Refresh
                    DrawerMenuItem(
                        title = "Refresh",
                        subtitle = "Refresh the screen",
                        icon = Icons.Filled.Refresh,
                        modifier = Modifier.focusRequester(firstItemFocusRequester)
                    ) {
                        scope.launch { drawerState.close() }
                        // Using the remembered WebView to reload!
                        webView.reload()
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    // 2. Orientation
                    DrawerMenuItem(
                        title = "Rotate Screen",
                        subtitle = "Toggle next orientation",
                        icon = ImageVector.vectorResource(R.drawable.ic_screen_rotation)
                    ) {
                        scope.launch { drawerState.close() }

                        // Just check the current state and jump to the next one
                        val nextOrientation = when (context?.requestedOrientation) {
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                            else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        }

                        onOrientationChange(nextOrientation)
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    // 3. Settings
                    DrawerMenuItem(
                        title = "Settings",
                        subtitle = "Change Your Preference",
                        icon = Icons.Filled.Settings,
                    ) {
                        scope.launch {
                            drawerState.close()
                            navController.navigate(MenuScreen.SETTINGS_SCREEN)
                        }
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    DrawerMenuItem(
                        title = "About",
                        subtitle = "Application Information",
                        icon = Icons.Filled.Info
                    ) {
                        scope.launch { drawerState.close() }
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    DrawerMenuItem(
                        title = "Exit",
                        subtitle = "Close the application",
                        icon = Icons.Filled.Close
                    ) {
                        scope.launch { context?.finishAffinity() }
                    }
                }
            }
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { webView },
                update = { view ->
                    // Update URL dynamically if changed in Settings
                    if (view.url != fullTargetUrl && view.url != null) {
                        view.loadUrl(fullTargetUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}