package com.example.demoapp

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.core.content.edit
import com.example.demoapp.ui.theme.BrandColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onIdSuccessfullySaved: () -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("MinarOSPrefs", Context.MODE_PRIVATE) }

    // 1. ADDED: Focus management controllers for the TV D-pad channel routing
    val focusManager = LocalFocusManager.current
    val saveButtonFocusRequester = remember { FocusRequester() }

    var mosqueIdInput by remember { mutableStateOf("") }
    var isInputError by remember { mutableStateOf(false) }

    var isFocused by remember { mutableStateOf(false) }

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
                // 2. FIXED: Configure the software keyboard to show a "Done" confirmation button
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                // 3. FIXED: When the user presses "Done" on the on-screen keyboard,
                // instantly force focus onto the save display button!
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
                        // Clear keyboard focus cleanly before migrating screens
                        focusManager.clearFocus()

                        sharedPrefs.edit {
                            putString("MOSQUE_ID", mosqueIdInput)
                        }
                        onIdSuccessfullySaved()
                    } else {
                        isInputError = true
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFocused) BrandColor else Color.LightGray,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(56.dp)
                    // 4. ADDED: Attach the focus requester pointer reference link here
                    .focusRequester(saveButtonFocusRequester)
                    .onFocusChanged { isFocused = it.isFocused }
            ) {
                Text(
                    text = "Save Display",
                    fontSize = 16.sp,
                    color = if (isFocused) Color.White else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}