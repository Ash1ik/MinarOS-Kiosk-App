package com.example.demoapp.menu.settings.sections

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.demoapp.ui.theme.BrandColor

@Composable
fun ConstraintsSection(
    isAlwaysOnEnabled: Boolean,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("MinarosPrefs", Context.MODE_PRIVATE)

    Column {
        Text("System Constraints", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("App Always On Mode", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                Text("Prevent TV backlight from dimming or sleeping automatically", fontSize = 14.sp, color = Color.Gray)
            }
            Switch(
                checked = isAlwaysOnEnabled,
                onCheckedChange = { value ->
                    // 1. Alert the state tracker in SettingsScreen
                    // 2. Alert the MainActivity window layers to change live
                    onAlwaysOnChanged(value)

                    // 3. Persist layout states onto disk
                    sharedPrefs.edit { putBoolean("ALWAYS_ON_MODE", value) }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BrandColor,
                    checkedTrackColor = BrandColor.copy(alpha = 0.4f)
                )
            )
        }
    }
}