package com.example.minaros.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minaros.data.MosqueDataManager
import com.example.minaros.screen.R
import com.example.minaros.ui.theme.BrandColor

/**
 * The initial onboarding screen displayed when the app has no saved Mosque ID.
 * * This screen is heavily optimized for Android TV / Fire OS navigation (D-Pad),
 * utilizing [MutableInteractionSource] to provide visual feedback when UI elements
 * are focused via a physical remote control.
 *
 * @param onIdSuccessfullySaved Callback triggered when a valid 6-digit ID is
 * securely stored in the local database, allowing the main router to proceed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onIdSuccessfullySaved: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val saveButtonFocusRequester = remember { FocusRequester() }

    var mosqueIdInput by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }

    // 🎯 TV FOCUS TRACKER: Reads hardware remote navigation state events cleanly.
    // This allows us to change the button color dynamically when the user hovers over it with a TV remote.
    val buttonInteractionSource = remember { MutableInteractionSource() }
    val isButtonFocused by buttonInteractionSource.collectIsFocusedAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(480.dp)
                .padding(24.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.ic_minaros_logo),
                contentDescription = "minarOS Logo",
                modifier = Modifier
                    .size(width = 240.dp, height = 120.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Enter Mosque ID",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = mosqueIdInput,
                onValueChange = { value ->
                    // Validation: Restrict to exactly 6 digits
                    if (value.length <= 6 && value.all { it.isDigit() }) {
                        mosqueIdInput = value
                        isInputError = false
                    }
                },
                placeholder = {
                    Text("Example: 123456", color = Color.Gray.copy(alpha = 0.7f))
                },
                singleLine = true,
                isError = isInputError,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // When the user clicks 'Done' on the keyboard, hide the keyboard
                        // and instantly snap the TV remote focus to the Save button
                        keyboardController?.hide()
                        saveButtonFocusRequester.requestFocus()
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrandColor,
                    unfocusedBorderColor = BrandColor.copy(alpha = 0.4f),
                    cursorColor = BrandColor,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    errorBorderColor = Color.Red
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            )

            Button(
                onClick = {
                    if (mosqueIdInput.length == 6) {
                        focusManager.clearFocus()
                        MosqueDataManager.saveMosqueId(context, mosqueIdInput)
                        onIdSuccessfullySaved()
                    } else {
                        // Triggers the red error border on the text field if the ID is too short
                        isInputError = true
                    }
                },
                interactionSource = buttonInteractionSource,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    // Dynamically swaps container and text colors based on TV remote D-pad highlighting
                    containerColor = if (isButtonFocused) BrandColor else Color.LightGray.copy(alpha = 0.4f),
                    contentColor = if (isButtonFocused) Color.White else Color.Gray
                ),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(56.dp)
                    .focusRequester(saveButtonFocusRequester)
            ) {
                Text(
                    text = "Save Display",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}