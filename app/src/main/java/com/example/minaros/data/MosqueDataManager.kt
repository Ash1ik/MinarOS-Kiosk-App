package com.example.minaros.data

import android.content.Context
import androidx.core.content.edit

/**
 * A centralized singleton responsible for managing the local persistent storage
 * of the application's configuration states, specifically the target Mosque ID.
 */
object MosqueDataManager {

    private const val PREFS_NAME = "MinarOSPrefs"
    private const val KEY_MOSQUE_ID = "TARGET_ENDPOINT"

    /**
     * Writes the fresh Mosque ID into disk storage.
     * This automatically overwrites any existing ID saved under the same key.
     *
     * @param context The application or activity context.
     * @param newId The validated 6-digit endpoint identifier.
     */
    fun saveMosqueId(context: Context, newId: String) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        sharedPrefs.edit(commit = true) {
            putString(KEY_MOSQUE_ID, newId.trim())
        }
    }

    /**
     * Fetches the saved active configuration endpoint address.
     *
     * @return The 6-digit Mosque ID, or an empty string if no profile is configured.
     */
    fun getMosqueId(context: Context): String {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPrefs.getString(KEY_MOSQUE_ID, "") ?: ""
    }

    /**
     * Completely purges the saved Mosque ID tracking entry from disk storage.
     * Use this to log out or reset the display back to factory onboarding configurations.
     */
    fun deleteMosqueId(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        sharedPrefs.edit(commit = true) {
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