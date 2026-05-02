package com.example.eureka.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.eureka.ui.theme.*

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var publicByDefault by remember { mutableStateOf(true) }
    var locationSharing by remember { mutableStateOf(true) }
    var notifications   by remember { mutableStateOf(false) }

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
                Text("Settings", style = MaterialTheme.typography.titleLarge, color = ColorOnSurface)
            }

            Spacer(Modifier.height(16.dp))

            SettingsToggle("Public drawings by default",    publicByDefault) { publicByDefault = it }
            SettingsToggle("Share precise location",        locationSharing) { locationSharing = it }
            SettingsToggle("Nearby drawing notifications",  notifications)   { notifications   = it }
        }
    }
}

@Composable
private fun SettingsToggle(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurface)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = ColorOnSurface)
        Switch(
            checked         = checked,
            onCheckedChange = onChecked,
            colors          = SwitchDefaults.colors(
                checkedThumbColor = ColorPrimary,
                checkedTrackColor = ColorPrimary.copy(alpha = 0.3f)
            )
        )
    }
    Spacer(Modifier.height(8.dp))
}
