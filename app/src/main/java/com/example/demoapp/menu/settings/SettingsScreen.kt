package com.example.demoapp.menu.settings

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.demoapp.MosqueDataManager
import com.example.demoapp.TvButton
import com.example.demoapp.menu.settings.sections.ConstraintsSection
import com.example.demoapp.menu.settings.sections.RotationSection
import com.example.demoapp.menu.settings.sections.StorageSection
import com.example.demoapp.ui.theme.BrandColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.text.isDigit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onOrientationChange: (Int) -> Unit,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs =
        remember { context.getSharedPreferences("MinarOSPrefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Focus Requesters
    val backButtonFocus = remember { FocusRequester() }
    val textFieldFocus = remember { FocusRequester() }
    val saveButtonFocus = remember { FocusRequester() }

    // Editing state
    var isEditing by remember { mutableStateOf(false) }
    var isFieldFocused by remember { mutableStateOf(false) }

    // State bindings
    var currentEndpoint by remember {
        mutableStateOf(MosqueDataManager.getMosqueId(context))
    }
    var isCacheEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("ENABLE_CACHE", false)) }
    var updateStatus by remember { mutableStateOf("App is up to date (v1.0)") }
    var selectedRotation by remember {
        mutableIntStateOf(
            sharedPrefs.getInt("SAVED_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        )
    }
    var isAlwaysOnEnabled by remember {
        mutableStateOf(sharedPrefs.getBoolean("ALWAYS_ON_MODE", true))
    }

    // Direct initial layout focus targeting
    LaunchedEffect(Unit) {
        delay(100)
        backButtonFocus.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "MinarOS Settings",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    var isBackFocused by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { onBackPressed() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .focusRequester(backButtonFocus)
                            .onFocusChanged { isBackFocused = it.isFocused }
                            // 🎯 HARDCODED DIRECTION: Forcibly point down arrow directly to the text field!
                            .focusProperties {
                                down = textFieldFocus
                            }
                            .background(
                                if (isBackFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrandColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                // 1. Mosque ID Selection
                Text(
                    text = "Mosque ID",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                currentEndpoint.let { endpointValue ->
                    OutlinedTextField(
                        value = endpointValue,
                        onValueChange = { value ->
                            // 🎯 FIX 1: Use 'value' instead of 'it' so characters actually get written to state
                            // Limits input box strictly to numbers up to 6 digits maximum
                            if (value.length <= 6 && value.all { it.isDigit() }) {
                                currentEndpoint = value
                            }
                        },
                        singleLine = true,
                        readOnly = false,
                        placeholder = {
                            Text("Enter 6-digit ID", color = Color.Gray.copy(alpha = 0.6f))
                        },
                        // 🎯 FIX 2: Dynamically flags an error border if the user stops typing before hitting 6 digits
                        isError = endpointValue.isNotEmpty() && endpointValue.length < 6,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                // Only allow moving forward if the validation constraint is perfectly satisfied
                                if (endpointValue.length == 6) {
                                    keyboardController?.hide()
                                    saveButtonFocus.requestFocus()
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(textFieldFocus)
                            .onFocusChanged { focusState ->
                                isFieldFocused = focusState.isFocused
                                if (!focusState.isFocused) {
                                    keyboardController?.hide()
                                }
                            }
                            .focusProperties {
                                up = backButtonFocus
                                down = saveButtonFocus
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.LightGray.copy(alpha = 0.3f),
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = BrandColor,
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.DarkGray,
                            errorBorderColor = Color.Red
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                TvButton(
                    text = "Save Configuration",
                    modifier = Modifier
                        .focusRequester(saveButtonFocus)
                        // 🎯 HARDCODED DIRECTION: Route up directly back to the text field!
                        .focusProperties {
                            up = textFieldFocus
                        },
                    onClick = {
                        sharedPrefs.edit { putString("TARGET_ENDPOINT", currentEndpoint) }
                        Toast.makeText(context, "Mosque id is saving...", Toast.LENGTH_SHORT).show()
                    }
                )

                SectionDivider()

                // 2. Web Caching Setting
                Text(
                    "Web Caching",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                var isCacheRowFocused by remember { mutableStateOf(false) }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isCacheRowFocused = it.isFocused }
                        .focusable()
                        .onKeyEvent { keyEvent ->
                            if (isCacheRowFocused &&
                                keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                        keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                            ) {

                                val nextState = !isCacheEnabled
                                isCacheEnabled = nextState
                                sharedPrefs.edit { putBoolean("ENABLE_CACHE", nextState) }

                                if (nextState) {
                                    Toast.makeText(
                                        context,
                                        "Web Caching is ENABLED",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Web Caching is DISABLED",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                return@onKeyEvent true
                            }
                            false
                        }
                        .clickable {
                            val nextState = !isCacheEnabled
                            isCacheEnabled = nextState
                            sharedPrefs.edit { putBoolean("ENABLE_CACHE", nextState) }

                            if (nextState) {
                                Toast.makeText(
                                    context,
                                    "Web Caching is ENABLED",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Web Caching is DISABLED",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .background(
                            if (isCacheRowFocused) Color.LightGray else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (isCacheEnabled) "Web Caching is ENABLED" else "Web Caching is DISABLED",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Switch(
                        checked = isCacheEnabled,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BrandColor,
                            checkedTrackColor = BrandColor.copy(alpha = 0.4f)
                        )
                    )
                }

                SectionDivider()

                // 3. Screen Rotation Section
                RotationSection(
                    selectedRotation = selectedRotation,
                    onRotationSelected = {
                        selectedRotation = it
                        onOrientationChange(selectedRotation)
                    },
                )

                SectionDivider()

                // 4. Clear Cache Memory Section
                StorageSection()

                SectionDivider()

                // 5. App Always-On Mode Section
                ConstraintsSection(
                    isAlwaysOnEnabled = isAlwaysOnEnabled,
                    onAlwaysOnChanged = {
                        isAlwaysOnEnabled = it
                        onAlwaysOnChanged(it)
                    }
                )

                SectionDivider()

                // 6. System Updates
                Text(
                    "System Updates",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TvButton(
                        text = "Check for Updates",
                        onClick = {
                            scope.launch {
                                updateStatus = "Checking remote server..."
                                delay(1500)
                                updateStatus = "App is up to date (v1.0)"
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(updateStatus, color = Color.DarkGray, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun SectionDivider() {
    Column {
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.6f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))
    }
}