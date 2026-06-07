package com.example.minaros.ui.screens.settings.sections

import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minaros.ui.components.TvButton
import com.example.minaros.ui.theme.BrandColor

/**
 * A manual utility section allowing the user to purge localized WebView caches.
 * Useful if the React/Vue frontend assets get stuck or corrupted on the local disk.
 */
@Composable
fun StorageSection() {
    val context = LocalContext.current

    Column {
        Text("Storage Maintenance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Clear locally stored website assets if things look out of date", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        TvButton(
            text = "Delete Cache Memory",
            onClick = {
                try {
                    // Generates a temporary WebView instance strictly to execute the global wipe command
                    val webView = WebView(context)
                    webView.clearCache(true)
                    Toast.makeText(context, "Cache memory purged successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Clear task failed. Please retry.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
}