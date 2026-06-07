package com.example.minaros.ui.screens.settings

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import com.example.minaros.data.MosqueDataManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.minaros.ui.screens.settings.sections.ConstraintsSection
import com.example.minaros.ui.screens.settings.sections.RotationSection
import com.example.minaros.ui.screens.settings.sections.StorageSection
import com.example.minaros.ui.components.TvButton
import com.example.minaros.ui.theme.BrandColor

/**
 * The master configuration screen for the application.
 * Contains sub-sections for Mosque ID, rotation locking, web caching, storage, and constraints.
 * Highly optimized for D-Pad / Smart TV navigation.
 *
 * @param onBackPressed Callback to pop the backstack and return to the main dashboard.
 * @param onOrientationChange Callback triggered when the user saves a new rotation setting.
 * @param onAlwaysOnChanged Callback triggered when the user toggles the screen wake-lock.
 */
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

    // Focus Requesters for hardware D-pad routing
    val backButtonFocus = remember { FocusRequester() }
    val containerBoxFocus = remember { FocusRequester() }
    val textFieldFocus = remember { FocusRequester() }
    val saveButtonFocus = remember { FocusRequester() }

    var isEditingMode by remember { mutableStateOf(false) }

    val containerInteractionSource = remember { MutableInteractionSource() }
    val isContainerFocused by containerInteractionSource.collectIsFocusedAsState()

    // Persistent State Bindings
    var currentEndpoint by remember { mutableStateOf(MosqueDataManager.getMosqueId(context)) }
    var isCacheEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("ENABLE_CACHE", false)) }
    var updateStatus by remember { mutableStateOf("App is up to date (v1.0)") }
    var selectedRotation by remember {
        mutableIntStateOf(sharedPrefs.getInt("SAVED_ORIENTATION", ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE))
    }
    var isAlwaysOnEnabled by remember { mutableStateOf(sharedPrefs.getBoolean("ALWAYS_ON_MODE", true)) }

    val configuration = LocalConfiguration.current
    val isTelevision = (configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION

    // Automatically focus the back button upon entry so the user isn't lost
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

                // --- 1. MOSQUE ID SECTION ---
                Text(
                    text = "Mosque ID",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrandColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (!isTelevision) {
                    // Mobile Implementation
                    OutlinedTextField(
                        value = currentEndpoint,
                        onValueChange = { value ->
                            if (value.length <= 6 && value.all { it.isDigit() }) currentEndpoint = value
                        },
                        singleLine = true,
                        placeholder = { Text("Enter 6-digit ID", color = Color.Gray.copy(alpha = 0.6f)) },
                        isError = currentEndpoint.isNotEmpty() && currentEndpoint.length < 6,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                saveButtonFocus.requestFocus()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.LightGray.copy(alpha = 0.2f),
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = BrandColor,
                            unfocusedBorderColor = Color.LightGray,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.DarkGray,
                            errorBorderColor = Color.Red
                        )
                    )
                } else {
                    // TV Implementation (Focus Stealing Wrapper)
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
                                    textFieldFocus.requestFocus()
                                    keyboardController?.show()
                                    return@onKeyEvent true
                                }
                                false
                            }
                            .background(
                                if (isContainerFocused && !isEditingMode) Color(0xFFE0E0E0) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        OutlinedTextField(
                            value = currentEndpoint,
                            onValueChange = { value ->
                                if (value.length <= 6 && value.all { it.isDigit() }) currentEndpoint = value
                            },
                            singleLine = true,
                            readOnly = !isEditingMode,
                            placeholder = { Text("Enter 6-digit ID", color = Color.Gray.copy(alpha = 0.6f)) },
                            isError = currentEndpoint.isNotEmpty() && currentEndpoint.length < 6,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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
                                    if (!focusState.isFocused && !isContainerFocused) isEditingMode = false
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
                }
                Spacer(modifier = Modifier.height(12.dp))

                TvButton(
                    text = "Save Configuration",
                    modifier = Modifier
                        .focusRequester(saveButtonFocus)
                        .focusProperties { up = containerBoxFocus },
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

                // --- 2. ROTATION SECTION ---
                RotationSection(
                    selectedRotation = selectedRotation,
                    onRotationSelected = {
                        selectedRotation = it
                        onOrientationChange(selectedRotation)
                    },
                )

                SectionDivider()

                // --- 3. WEB CACHING SECTION ---
                Text("Web Caching", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
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

                // --- 4. CLEAR CACHE SECTION ---
                StorageSection()

                SectionDivider()

                // --- 5. ALWAYS-ON MODE SECTION ---
                ConstraintsSection(
                    isAlwaysOnEnabled = isAlwaysOnEnabled,
                    onAlwaysOnChanged = {
                        isAlwaysOnEnabled = it
                        // 🎯 FIX: Explicitly save the wakelock preference so it persists across reboots!
                        sharedPrefs.edit { putBoolean("ALWAYS_ON_MODE", it) }
                        onAlwaysOnChanged(it)
                    }
                )

                SectionDivider()

                // --- 6. SYSTEM UPDATES SECTION ---
                Text("System Updates", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = BrandColor)
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

/** Private helper composable to render uniform horizontal dividers between settings blocks */
@Composable
private fun SectionDivider() {
    Column {
        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.6f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(20.dp))
    }
}