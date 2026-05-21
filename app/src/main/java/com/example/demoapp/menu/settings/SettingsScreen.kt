package com.example.demoapp.menu.settings

import android.content.Context
import android.content.pm.ActivityInfo
import android.view.KeyEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackPressed: () -> Unit,
    onOrientationChange: (Int) -> Unit,
    onAlwaysOnChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("MinarOSPrefs", Context.MODE_PRIVATE) }
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Focus Requesters
    val backButtonFocus = remember { FocusRequester() }
    val containerBoxFocus = remember { FocusRequester() }
    val textFieldFocus = remember { FocusRequester() }
    val saveButtonFocus = remember { FocusRequester() }

    // Intercept states
    var isEditingMode by remember { mutableStateOf(false) }

    // Interaction sources to listen to baseline TV focus state events
    val containerInteractionSource = remember { MutableInteractionSource() }
    val isContainerFocused by containerInteractionSource.collectIsFocusedAsState()

    // State bindings
    var currentEndpoint by remember { mutableStateOf(MosqueDataManager.getMosqueId(context)) }
    var isCacheEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("ENABLE_CACHE", false)) }
    var updateStatus by remember { mutableStateOf("App is up to date (v1.0)") }
    var selectedRotation by remember {
        mutableIntStateOf(sharedPrefs.getInt("SAVED_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE))
    }
    var isAlwaysOnEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("ALWAYS_ON_MODE", true)) }

    LaunchedEffect(Unit) {
        delay(100)
        backButtonFocus.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "MinarOS Settings", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    var isBackFocused by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { onBackPressed() },
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .focusRequester(backButtonFocus)
                            .onFocusChanged { isBackFocused = it.isFocused }
                            .focusProperties { down = containerBoxFocus }
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

                Text(
                    text = "Mosque ID",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                // 🎯 FIXED WRAPPER CONTAINER: Focusable at all times to prevent hardware back-snapping
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(containerBoxFocus)
                        .focusProperties {
                            up = backButtonFocus
                            down = saveButtonFocus
                        }
                        .focusable(enabled = true, interactionSource = containerInteractionSource)
                        .onKeyEvent { keyEvent ->
                            if (!isEditingMode && isContainerFocused &&
                                keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN &&
                                (keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER ||
                                        keyEvent.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER)
                            ) {
                                isEditingMode = true
                                // Hand over focus to the inner field before popping open the IME
                                textFieldFocus.requestFocus()
                                keyboardController?.show()
                                return@onKeyEvent true
                            }
                            false
                        }
                        // 🎯 HOVER COLOR CHANGED: Uses a clean, clear Gray overlay during focus tracking passes
                        .background(
                            if (isContainerFocused && !isEditingMode) Color(0xFFE0E0E0) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    OutlinedTextField(
                        value = currentEndpoint,
                        onValueChange = { value ->
                            if (value.length <= 6 && value.all { it.isDigit() }) {
                                currentEndpoint = value
                            }
                        },
                        singleLine = true,
                        readOnly = !isEditingMode,
                        placeholder = {
                            Text("Enter 6-digit ID", color = Color.Gray.copy(alpha = 0.6f))
                        },
                        isError = currentEndpoint.isNotEmpty() && currentEndpoint.length < 6,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (currentEndpoint.length == 6) {
                                    isEditingMode = false
                                    keyboardController?.hide()
                                    saveButtonFocus.requestFocus()
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(textFieldFocus)
                            .onFocusChanged { focusState ->
                                if (!focusState.isFocused && !isContainerFocused) {
                                    isEditingMode = false
                                }
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = if (isEditingMode) Color.LightGray.copy(alpha = 0.2f) else Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = if (isEditingMode) BrandColor else Color.LightGray,
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
                        .focusProperties {
                            up = containerBoxFocus
                        },
                    onClick = {
                        if (currentEndpoint.length == 6) {
                            MosqueDataManager.saveMosqueId(context, currentEndpoint)
                            Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Error: Must be exactly 6 digits.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

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
                                return@onKeyEvent true
                            }
                            false
                        }
                        .clickable {
                            val nextState = !isCacheEnabled
                            isCacheEnabled = nextState
                            sharedPrefs.edit { putBoolean("ENABLE_CACHE", nextState) }
                        }
                        .background(
                            if (isCacheRowFocused) Color.LightGray.copy(alpha = 0.3f) else Color.Transparent,
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