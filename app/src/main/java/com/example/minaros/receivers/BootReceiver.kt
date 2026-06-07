package com.example.minaros.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.minaros.MainActivity

/**
 * A system-level listener that waits for the device hardware to finish its cold boot sequence.
 * When triggered, it automatically forces the MinarOS application to launch into the foreground.
 * This is critical for "kiosk mode" setups where the display should run unattended after a power loss.
 *
 * NOTE: Ensure the AndroidManifest.xml includes both the RECEIVE_BOOT_COMPLETED permission
 * and registers this receiver with the correct package path.
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Verify the broadcast strictly matches the system boot completion event
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                Toast.makeText(context, "MinarOS: System Boot Completed. Starting Display...", Toast.LENGTH_LONG).show()

                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    // 🎯 CRITICAL: FLAG_ACTIVITY_NEW_TASK is absolutely mandatory when
                    // starting an Activity from a background context like a BroadcastReceiver.
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                    // Clears any ghost instances of the app out of RAM to ensure a completely fresh boot state
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }

                context.startActivity(launchIntent)

            } catch (e: Exception) {
                // Failsafe: Prevents a system-level bootloop crash if the custom TV OS
                // temporarily restricts background activity launches during heavy boot loads.
                Log.e("BootReceiver", "Critical Failure: Could not auto-start MinarOS display", e)
            }
        }
    }
}