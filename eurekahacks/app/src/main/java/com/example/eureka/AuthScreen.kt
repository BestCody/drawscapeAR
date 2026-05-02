package com.example.eureka

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onBackToDraw: () -> Unit
) {
    var isSignup by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── BASE: solid black ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        // ── BACKGROUND IMAGE (oversized + bottom cropped) ──
        Image(
            painter = painterResource(id = R.drawable.auth_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(1f) // makes image extend beyond screen bottom
                .alpha(0.28f)
        )

        // ── UI LAYER ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {

            // ── Tabs ──
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = { isSignup = true }) {
                        Text("Sign Up", color = Color.White)
                    }
                    if (isSignup) {
                        Box(
                            Modifier
                                .width(48.dp)
                                .height(2.dp)
                                .background(Color.White)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(onClick = { isSignup = false }) {
                        Text("Login", color = Color.White)
                    }
                    if (!isSignup) {
                        Box(
                            Modifier
                                .width(48.dp)
                                .height(2.dp)
                                .background(Color.White)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── LARGE LOGO (does NOT push layout too far) ──
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // fixed space so it doesn’t push fields down
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(240.dp) // bigger logo
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── USERNAME ──
            Text("Username", color = Color.White)
            Spacer(Modifier.height(8.dp))

            AuthInputField(
                value = username,
                onValueChange = { username = it }
            )

            Spacer(Modifier.height(14.dp))

            // ── PASSWORD (sits visually over background region) ──
            Text("Password", color = Color.White)
            Spacer(Modifier.height(8.dp))

            AuthInputField(
                value = password,
                onValueChange = { password = it }
            )

            Spacer(Modifier.height(22.dp))

            // ── ACTION BUTTON ──
            Button(
                onClick = onLoginSuccess,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Text(if (isSignup) "Sign Up" else "Login")
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onBackToDraw,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.DarkGray,
                    contentColor = Color.White
                )
            ) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray, RoundedCornerShape(12.dp))
            .border(
                1.dp,
                Color.White.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Color.White),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier.fillMaxWidth()
        )
    }
}