package com.example.demoapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Check if the system is telling us it just finished booting
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val launchIntent = Intent(context, MainActivity::class.java)

            Toast.makeText(context, "TV TURNED ON: MinarOS Auto-Starting...", Toast.LENGTH_LONG).show()

            // FLAG_ACTIVITY_NEW_TASK is required because we are starting the app
            // from the background, not from inside another activity.
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(launchIntent)
        }
    }
}