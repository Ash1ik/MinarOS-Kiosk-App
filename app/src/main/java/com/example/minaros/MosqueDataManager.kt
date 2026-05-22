package com.example.minaros

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
            // 🎯 Step 1: Explicitly purge the previous entry out of memory blocks first
            remove(getMosqueId(context))

            // Step 2: Write the clean, validated 6-digit configuration entry safely
            putString(KEY_MOSQUE_ID, newId.trim())
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
     * Completely purges the saved Mosque ID tracking entry from disk storage.
     * Resets the application profile state back to factory onboarding configurations.
     */
    fun deleteMosqueId(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        sharedPrefs.edit(commit = true) {
            // 🎯 Atomic removal: Targets the unique key reference and wipes it completely
            remove(KEY_MOSQUE_ID)
        }
    }

    /**
     * Utility evaluation handle checking if the display hardware has completed raw setup profiles.
     */
    fun isAppConfigured(context: Context): Boolean {
        return getMosqueId(context).isNotEmpty()
    }
}