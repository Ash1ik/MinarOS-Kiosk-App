package com.example.minaros.menu.settings.sections

import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minaros.ui.theme.BrandColor

@Composable
fun ConstraintsSection(
    isAlwaysOnEnabled: Boolean,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Text("Screen Constraints", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
    Spacer(modifier = Modifier.height(8.dp))

    var isRowFocused by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { isRowFocused = it.isFocused }
            .focusable()
            // 🎯 FIXED: Run Toast directly inside the native D-pad remote click handler pipeline
            .onKeyEvent { keyEvent ->
                if (isRowFocused &&
                    keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                    (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                            keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)) {

                    val nextState = !isAlwaysOnEnabled
                    onAlwaysOnChanged(nextState)

                    // 💡 Notice we use 'nextState' here to guarantee the message accurately reflects the upcoming UI update
                    if (nextState) {
                        Toast.makeText(context, "Keep Screen Wake Lock ACTIVE", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Screen Wake Lock INACTIVE", Toast.LENGTH_SHORT).show()
                    }
                    return@onKeyEvent true
                }
                false
            }
            .clickable {
                val nextState = !isAlwaysOnEnabled
                onAlwaysOnChanged(nextState)

                if (nextState) {
                    Toast.makeText(context, "Keep Screen Wake Lock ACTIVE", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Screen Wake Lock INACTIVE", Toast.LENGTH_SHORT).show()
                }
            }
            .background(
                if (isRowFocused) Color.LightGray else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = if (isAlwaysOnEnabled) "Keep Screen Wake Lock ACTIVE" else "Screen Wake Lock INACTIVE",
            color = Color.DarkGray,
            fontSize = 14.sp
        )
        Switch(
            checked = isAlwaysOnEnabled, // 🛠️ FIXED syntax error assignment hole here
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BrandColor,
                checkedTrackColor = BrandColor.copy(alpha = 0.4f)
            )
        )
    }
}