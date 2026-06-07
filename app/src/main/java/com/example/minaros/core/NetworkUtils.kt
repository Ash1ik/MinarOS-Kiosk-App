package com.example.minaros.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * A utility object for system-level hardware checks.
 */
object NetworkUtils {

    /**
     * Synchronously checks if the device currently has an active internet connection.
     * * @param context The application or activity context.
     * @return True if connected to WiFi/Ethernet with internet capability, false otherwise.
     */
    fun checkInternetConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}