package com.example.minaros.ui.screens.dashboard

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import com.example.minaros.screen.R
import com.example.minaros.core.NetworkUtils
import com.example.minaros.bridge.NativeNetworkCache
import com.example.minaros.data.MosqueDataManager
import com.example.minaros.ui.components.DrawerMenuItem
import com.example.minaros.ui.navigation.MenuScreen
import com.example.minaros.ui.theme.BrandColor


// 🎯 GLOBAL STATIC WORKSPACE BUFFER LOCK
// Isolates instance rendering out of local layout composition lifecycles completely
@SuppressLint("StaticFieldLeak")
private var persistentSystemWebView: WebView? = null

/**
 * The primary dashboard view of the MinarOS application.
 * Manages the persistent WebView instance, network state observation, and the side navigation drawer.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val context2 = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sharedPrefs = remember { context?.getSharedPreferences("MinarOSPrefs", Context.MODE_PRIVATE) }

    val firstItemFocusRequester = remember { FocusRequester() }

    var webViewProgress by remember { mutableIntStateOf(0) }
    var isUrlLoaded by remember { mutableStateOf(false) }
    var isNetworkOnline by remember {
        mutableStateOf(context?.let { NetworkUtils.checkInternetConnection(it) } ?: true)
    }

    var savedEndpoint by remember { mutableStateOf(MosqueDataManager.getMosqueId(context2)) }

    // Automatically re-fetches the latest ID every single time this screen comes into focus
    LaunchedEffect(Unit) {
        val freshId = MosqueDataManager.getMosqueId(context2)
        if (freshId != savedEndpoint) {
            savedEndpoint = freshId
        }
    }

    val fullTargetUrl = remember(savedEndpoint) { "https://minaros.com/$savedEndpoint" }

    // 🎯 WORKSPACE ENGINE MANAGER: Safe abstraction pattern wrapper stops duplicate provider collisions
    val webView = remember(context) {
        if (persistentSystemWebView == null) {
            try {
                persistentSystemWebView = WebView(context!!.applicationContext).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true

                    setBackgroundColor(android.graphics.Color.WHITE)

                    webViewClient = object : WebViewClient() {
                        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                            super.onReceivedError(view, request, error)
                            if (request?.isForMainFrame == true) webViewProgress = 100
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (!persistentSystemWebView?.url.isNullOrEmpty() && persistentSystemWebView?.url != "about:blank") {
                webViewProgress = 100
                isUrlLoaded = true
            }
        }

        persistentSystemWebView?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                webViewProgress = newProgress
            }
        }

        persistentSystemWebView
    }

    // Cold-boot layout loading hook
    LaunchedEffect(fullTargetUrl, isNetworkOnline) {
        if (webView != null && isNetworkOnline && fullTargetUrl.isNotEmpty()) {
            webViewProgress = 0
            webView.loadUrl(fullTargetUrl)
        } else if (!isNetworkOnline) {
            webViewProgress = 100
        }
    }

    // Hardware connection state tracking pipeline
    DisposableEffect(context) {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch {
                    isNetworkOnline = true
                    context.runOnUiThread {
                        webView?.let { currentView ->
                            if (!isUrlLoaded) {
                                webViewProgress = 0
                                currentView.loadUrl(fullTargetUrl)
                                isUrlLoaded = true
                            } else {
                                webViewProgress = 100
                                currentView.reload()
                            }
                        }
                    }
                }
            }

            override fun onLost(network: Network) {
                scope.launch { isNetworkOnline = false }
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)

        onDispose {
            try { connectivityManager.unregisterNetworkCallback(networkCallback) } catch (e: Exception) {}
        }
    }

    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    val exitThreshold = 500L
    val minDelay = 100L

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            try { firstItemFocusRequester.requestFocus() } catch (e: Exception) {}
        } else {
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

        if (webView != null && webView.canGoBack()) {
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
                        modifier = Modifier.size(120.dp).padding(start = 24.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                HorizontalDivider(color = Color.LightGray, thickness = 1.dp)

                // --- DRAWER ITEMS ---
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                ) {
                    DrawerMenuItem(
                        title = "Refresh",
                        subtitle = "Refresh the screen",
                        icon = ImageVector.vectorResource(R.drawable.ic_refresh),
                        modifier = Modifier.focusRequester(firstItemFocusRequester)
                    ) {
                        if (isNetworkOnline) {
                            webView?.reload()
                        }
                        updatedOrientation?.let { onOrientationChange(it) }
                        scope.launch { drawerState.close() }
                    }

                    HorizontalDivider(color = Color(0xFFEEEEEE), thickness = 1.dp)

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
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

            if (webView != null) {
                AndroidView(
                    factory = {
                        (webView.parent as? ViewGroup)?.removeView(webView)
                        webView
                    },
                    update = { view ->
                        view.settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                        }

                        // Attach the Network Cache Bridge
                        view.addJavascriptInterface(NativeNetworkCache(context2), "AndroidNetworkMock")

                        val isCacheEnabledInSettings = sharedPrefs?.getBoolean("ENABLE_CACHE", false) ?: false
                        val currentStoredEndpoint = MosqueDataManager.getMosqueId(context2)
                        val freshTargetUrl = "https://minaros.com/$currentStoredEndpoint"

                        view.settings.cacheMode = if (isCacheEnabledInSettings && !isNetworkOnline) {
                            WebSettings.LOAD_CACHE_ELSE_NETWORK
                        } else {
                            WebSettings.LOAD_DEFAULT
                        }

                        // The Network Lifecycle Interceptor
                        view.webViewClient = object : WebViewClient() {
                            override fun onReceivedError(v: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                super.onReceivedError(v, request, error)
                                if (request?.isForMainFrame == true) webViewProgress = 100
                            }

                            override fun onPageFinished(pageView: WebView?, url: String?) {
                                super.onPageFinished(pageView, url)
                                pageView?.clearHistory()
                            }

                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)

                                // Proxy Script: Intercepts JS Fetch/XHR calls
                                val jsProxy = """
                                    (function() {
                                        if (window.__networkMockInjected) return;
                                        window.__networkMockInjected = true;

                                        const originalFetch = window.fetch;
                                        window.fetch = async function(...args) {
                                            const reqUrl = typeof args[0] === 'string' ? args[0] : (args[0] ? args[0].url : 'unknown');
                                            try {
                                                const response = await originalFetch.apply(this, args);
                                                const clone = response.clone();
                                                clone.text().then(text => {
                                                    if (text && (text.startsWith('{') || text.startsWith('['))) {
                                                        window.AndroidNetworkMock.savePayload(reqUrl, text);
                                                    }
                                                }).catch(e => {});
                                                return response;
                                            } catch (err) {
                                                const cached = window.AndroidNetworkMock.getPayload(reqUrl);
                                                if (cached) {
                                                    return new Response(cached, {
                                                        status: 200,
                                                        headers: { 'Content-Type': 'application/json' }
                                                    });
                                                }
                                                throw err; 
                                            }
                                        };

                                        const originalXhrOpen = XMLHttpRequest.prototype.open;
                                        const originalXhrSend = XMLHttpRequest.prototype.send;
                                        XMLHttpRequest.prototype.open = function(method, url) {
                                            this._url = url;
                                            originalXhrOpen.apply(this, arguments);
                                        };
                                        XMLHttpRequest.prototype.send = function() {
                                            const cached = window.AndroidNetworkMock.getPayload(this._url);
                                            if (!navigator.onLine && cached) {
                                                Object.defineProperty(this, 'responseText', { value: cached, writable: true });
                                                Object.defineProperty(this, 'status', { value: 200, writable: true });
                                                Object.defineProperty(this, 'readyState', { value: 4, writable: true });
                                                setTimeout(() => {
                                                    if (this.onreadystatechange) this.onreadystatechange();
                                                    if (this.onload) this.onload();
                                                }, 50);
                                                return;
                                            }
                                            
                                            this.addEventListener('load', function() {
                                                const text = this.responseText;
                                                if (text && (text.startsWith('{') || text.startsWith('['))) {
                                                    window.AndroidNetworkMock.savePayload(this._url, text);
                                                }
                                            });
                                            originalXhrSend.apply(this, arguments);
                                        };
                                    })();
                                """.trimIndent()

                                view?.evaluateJavascript(jsProxy, null)
                            }
                        }

                        if (isNetworkOnline) {
                            if (view.url != null && view.url != freshTargetUrl && view.url != "$freshTargetUrl/") {
                                webViewProgress = 0
                                isUrlLoaded = false
                                view.loadUrl(freshTargetUrl)
                            }
                        } else {
                            if (isCacheEnabledInSettings && (view.url == null || view.url == "about:blank")) {
                                webViewProgress = 0
                                view.loadUrl(freshTargetUrl)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Full-screen white curtain loading layer
            if (webViewProgress < 100 && isNetworkOnline) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(64.dp),
                            color = BrandColor,
                            strokeWidth = 5.dp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Mosque Display is Loading...",
                            color = BrandColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}