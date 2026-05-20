package com.example.demoapp

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebChromeClient
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    currentOrientation: Int,
    navController: NavController,
    onOrientationChange: (Int) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current as? Activity
    val focusManager = LocalFocusManager.current
    val sharedPrefs = remember { context?.getSharedPreferences("MinarOSPrefs", Context.MODE_PRIVATE) }

    val firstItemFocusRequester = remember { FocusRequester() }

    // Read the endpoint from settings
    val savedEndpoint by remember {
        mutableStateOf(sharedPrefs?.getString("TARGET_ENDPOINT", "100001") ?: "100001")
    }

    // 🎯 FIX 1: Wrap the final URL inside remember(savedEndpoint).
    // This tells Compose to preserve this string across layout re-entries!
    val fullTargetUrl = remember(savedEndpoint) {
        "https://minaros.com/$savedEndpoint"
    }

    // State to track the precise loading percentage of the WebView
    var webViewProgress by remember { mutableIntStateOf(0) }

    // Optimization flag to control initialization constraints
    var isUrlLoaded by remember { mutableStateOf(false) }

    // Create and preserve the WebView instance cleanly
    val webView = remember {
        WebView(context!!).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            webViewClient = WebViewClient()
            setBackgroundColor(android.graphics.Color.WHITE)

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    webViewProgress = newProgress
                }
            }
        }
    }

    // 🎯 FIX 2: Check URL loading flag inside initialization hook.
    // Since fullTargetUrl is now remembered, this block will evaluate as completely idle on return layouts.
    LaunchedEffect(fullTargetUrl) {
        if (!isUrlLoaded) {
            webView.loadUrl(fullTargetUrl)
            isUrlLoaded = true
        }
    }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val exitThreshold = 500L
    val minDelay = 100L

    // Drawer Auto-Focus Logic
    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Open) {
            try { firstItemFocusRequester.requestFocus() } catch (e: Exception) { }
        } else if (drawerState.targetValue == DrawerValue.Closed) {
            focusManager.clearFocus()
        }
    }

    // --- UNIFIED BACK HANDLER ---
    BackHandler(enabled = true) {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastClick = currentTime - lastBackPressTime

        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
            lastBackPressTime = 0L
            return@BackHandler
        }

        if (webView.canGoBack()) {
            webView.goBack()
            lastBackPressTime = 0L
            return@BackHandler
        }

        if (timeSinceLastClick in minDelay..exitThreshold) {
            context?.finishAffinity()
        } else {
            lastBackPressTime = currentTime
            scope.launch { drawerState.open() }
        }
    }

    val updatedOrientation = sharedPrefs?.getInt("SAVED_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)

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
                }

                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                // --- DRAWER ITEMS ---
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Refresh
                    DrawerMenuItem(
                        title = "Refresh",
                        subtitle = "Refresh the screen",
                        icon = ImageVector.vectorResource(R.drawable.ic_refresh),
                        modifier = Modifier.focusRequester(firstItemFocusRequester)
                    ) {
                        updatedOrientation?.let { onOrientationChange(it) }
                        scope.launch { drawerState.close() }

                        webViewProgress = 0
                        webView.reload()
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    // 2. Orientation
                    DrawerMenuItem(
                        title = "Rotate Screen",
                        subtitle = "Toggle next orientation",
                        icon = ImageVector.vectorResource(R.drawable.ic_rotation)
                    ) {
                        scope.launch { drawerState.close() }

                        val nextOrientation = when (currentOrientation) {
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                            else -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        }

                        onOrientationChange(nextOrientation)
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    // 3. Settings
                    DrawerMenuItem(
                        title = "Settings",
                        subtitle = "Change Your Preference",
                        icon = ImageVector.vectorResource(R.drawable.ic_settings),
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
                        icon = ImageVector.vectorResource(R.drawable.ic_about)
                    ) {
                        scope.launch { drawerState.close() }
                        navController.navigate(MenuScreen.ABOUT_SCREEN)
                    }
                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

                    DrawerMenuItem(
                        title = "Exit",
                        subtitle = "Close the application",
                        icon = ImageVector.vectorResource(R.drawable.ic_exit)
                    ) {
                        scope.launch { context?.finishAffinity() }
                    }
                }
            }
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // The Primary WebView Surface
            AndroidView(
                factory = { webView },
                update = { view ->
                    // 🎯 FIX 3: Safe dynamic update comparison.
                    // Instead of checking string allocations directly, we pull from storage.
                    // If unchanged, this block remains idle and never forces a reload on back navigation!
                    val currentStoredEndpoint = sharedPrefs?.getString("TARGET_ENDPOINT", "100001") ?: "100001"
                    val freshTargetUrl = "https://minaros.com/$currentStoredEndpoint"

                    if (view.url != null && view.url != freshTargetUrl && view.url != "$freshTargetUrl/") {
                        webViewProgress = 0
                        isUrlLoaded = false
                        view.loadUrl(freshTargetUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Full-screen white curtain layer is displayed only during actual initial loads (< 100)
            if (webViewProgress < 100) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Mosque Display is Loading...",
                            color = BrandColor,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = BrandColor,
                            strokeWidth = 5.dp
                        )
                    }
                }
            }
        }
    }
}