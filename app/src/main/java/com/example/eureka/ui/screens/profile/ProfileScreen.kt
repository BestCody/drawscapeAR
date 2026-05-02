package com.example.eureka.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.eureka.ui.theme.*

@Composable
fun ProfileScreen(
    onOpenSettings : () -> Unit,
    onBack         : () -> Unit,
) {
    Scaffold(containerColor = ColorBackground) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier          = Modifier.statusBarsPadding().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = ColorOnSurface)
                }
                Text(
                    text     = "Profile",
                    style    = MaterialTheme.typography.titleLarge,
                    color    = ColorOnSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Outlined.Settings, "Settings", tint = ColorOnSurface)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier         = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ColorPrimary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = "K",
                        style = MaterialTheme.typography.displayLarge.copy(color = ColorPrimary)
                    )
                }
                Column {
                    Text("@kai",                    style = MaterialTheme.typography.titleLarge, color = ColorOnSurface)
                    Text("12 drawings · 348 views", style = MaterialTheme.typography.bodyMedium, color = ColorOnSurfaceMuted)
                }
            }
        }
    }
}
