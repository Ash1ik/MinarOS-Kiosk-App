package com.example.minaros.menu.about

import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.minaros.R
import com.example.minaros.ui.theme.BrandColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackPressed: () -> Unit
) {
    val backButtonFocus = remember { FocusRequester() }
    val contentScrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var isContentFocused by remember { mutableStateOf(false) }

    // Enforce initial D-pad focus onto the navigation back button immediately upon display entry
    LaunchedEffect(Unit) {
        delay(100)
        backButtonFocus.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "About Application",
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
                            .background(
                                if (isBackFocused) Color.White.copy(alpha = 0.2f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back Navigation",
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
                .background(Color.White)
                .verticalScroll(contentScrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier
                    .width(760.dp)
                    .padding(horizontal = 40.dp, vertical = 24.dp)
                    .onFocusChanged { isContentFocused = it.isFocused }
                    .focusable()
                    // 🎯 HARDWARE D-PAD SCROLL INTERCEPTOR:
                    // Manually animate the ScrollState up or down on hardware button ticks!
                    .onKeyEvent { keyEvent ->
                        if (isContentFocused && keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
                            when (keyEvent.nativeKeyEvent.keyCode) {
                                KeyEvent.KEYCODE_DPAD_DOWN -> {
                                    coroutineScope.launch {
                                        // Scroll down by 60dp smoothly per press tick
                                        contentScrollState.animateScrollTo(contentScrollState.value + 60)
                                    }
                                    return@onKeyEvent true // Consume the D-pad click event cleanly
                                }
                                KeyEvent.KEYCODE_DPAD_UP -> {
                                    coroutineScope.launch {
                                        // Scroll up safely. If we hit the absolute top, move focus back to the header back arrow!
                                        if (contentScrollState.value == 0) {
                                            backButtonFocus.requestFocus()
                                        } else {
                                            contentScrollState.animateScrollTo(contentScrollState.value - 60)
                                        }
                                    }
                                    return@onKeyEvent true
                                }
                            }
                        }
                        false
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // --- Brand Identity Illustration (Logo) ---
                Image(
                    painter = painterResource(id = R.drawable.ic_minaros_logo),
                    contentDescription = "MinarOS Official Logo Profile",
                    modifier = Modifier.size(width = 280.dp, height = 140.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // --- Sub-Header Title Statement ---
                Text(
                    text = "AN OPERATING SYSTEM FOR MOSQUE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF212529),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Description Paragraph ---
                Text(
                    text = "MinarOS is a specialized operating system built to streamline mosque time management. It provides accurate, automated prayer and Iqamah tracking, clean digital signage displays, and easy community announcements—all managed through an intuitive, reliable platform.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF495057),
                    textAlign = TextAlign.Center,
                    lineHeight = 28.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- Technical Support Contact Target Anchor ---
                Text(
                    text = "contact@minaros.com",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF1A80A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(36.dp))

                // --- Version Flag Anchor Details ---
                Text(
                    text = "Version: 1.0",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6C757D),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}