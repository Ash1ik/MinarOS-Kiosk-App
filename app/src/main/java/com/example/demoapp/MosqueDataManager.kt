package com.example.demoapp

import android.content.Context
import androidx.core.content.edit

object MosqueDataManager {
    // 🎯 Centralized constant key tracking map references safely
    private const val PREFS_NAME = "MinarOSPrefs"
    private const val KEY_MOSQUE_ID = "TARGET_ENDPOINT"

    /**
     * Atomically clears previous records and writes the fresh Mosque ID into disk storage.
     */
    fun saveMosqueId(context: Context, newId: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        sharedPrefs.edit(commit = true) {
            // 🎯 Step 1: Force remove any duplicate keys or stale data blocks first
            remove(KEY_MOSQUE_ID)

            // Step 2: Inject the clean new user input string value securely
            if (newId.isNotBlank()) {
                putString(KEY_MOSQUE_ID, newId.trim())
            }
        }
    }

    /**
     * Fetches the saved active configuration endpoint string address.
     * Returns an empty string if no profile configuration data is currently active on disk.
     */
    fun getMosqueId(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_MOSQUE_ID, "") ?: ""
    }

    /**
     * Utility evaluation handle checking if the display hardware has completed raw setup profiles.
     */
    fun isAppConfigured(context: Context): Boolean {
        return getMosqueId(context).isNotEmpty()
    }
}