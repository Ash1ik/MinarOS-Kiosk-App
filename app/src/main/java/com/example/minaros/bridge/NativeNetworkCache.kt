package com.example.minaros.bridge

import android.content.Context
import android.content.SharedPreferences
import android.webkit.JavascriptInterface
import androidx.core.content.edit

/**
 * Acts as a bidirectional communication tunnel between the Android system and the WebView's JavaScript engine.
 * Used primarily to intercept live JSON API responses and save them securely to the physical disk,
 * allowing the React/Vue frontend to seamlessly load data even when completely offline.
 */
class NativeNetworkCache(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("MinarOSPrefs", Context.MODE_PRIVATE)

    /**
     * Called by the injected JavaScript proxy to save an intercepted network response natively.
     */
    @JavascriptInterface
    fun savePayload(url: String, data: String) {
        // Creates a safe, alphanumeric storage key from the URL string
        val safeUrl = url.takeLast(50).replace("[^a-zA-Z0-9]".toRegex(), "_")
        prefs.edit { putString("API_$safeUrl", data) }
    }

    /**
     * Called by the injected JavaScript proxy when an offline fetch fails,
     * returning the natively cached JSON string to keep the website layout intact.
     */
    @JavascriptInterface
    fun getPayload(url: String): String? {
        val safeUrl = url.takeLast(50).replace("[^a-zA-Z0-9]".toRegex(), "_")
        return prefs.getString("API_$safeUrl", null)
    }
}