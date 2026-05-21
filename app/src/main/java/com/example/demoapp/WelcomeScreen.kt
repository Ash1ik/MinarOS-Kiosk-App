package com.example.demoapp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.demoapp.ui.theme.BrandColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onIdSuccessfullySaved: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val saveButtonFocusRequester = remember { FocusRequester() }

    var mosqueIdInput by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }

    // 🎯 TV FOCUS TRACKER: Reads hardware remote navigation state events cleanly
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
                        isInputError = true
                    }
                },
                // 🎯 Pass our custom hardware interaction stream right here
                interactionSource = buttonInteractionSource,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    // Dynamically swaps backdrops and inner texts based on D-pad highlight rules
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