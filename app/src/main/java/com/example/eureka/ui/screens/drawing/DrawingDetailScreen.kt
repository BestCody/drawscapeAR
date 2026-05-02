package com.example.eureka.ui.screens.drawing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.eureka.ui.theme.*

@Composable
fun DrawingDetailScreen(
    drawingId  : String,
    onViewInAR : () -> Unit,
    onBack     : () -> Unit,
) {
    Scaffold(
        containerColor = ColorBackground,
        topBar = {
            Row(
                modifier          = Modifier.statusBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = ColorOnSurface)
                }
                Text(
                    text     = "Drawing #$drawingId",
                    style    = MaterialTheme.typography.titleLarge,
                    color    = ColorOnSurface,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Button(
                    onClick  = onViewInAR,
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(16.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = ColorPrimary)
                ) {
                    Icon(Icons.Outlined.ViewInAr, null)
                    Spacer(Modifier.width(8.dp))
                    Text("View in AR", color = ColorBackground)
                }
            }
        }
    ) { padding ->
        Column(
            modifier            = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(ColorSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Brush, "Preview", tint = ColorOnSurfaceMuted, modifier = Modifier.size(48.dp))
            }

            InfoRow(Icons.Outlined.Person,     "Author",     "@void_ink")
            InfoRow(Icons.Outlined.LocationOn, "Location",   "Main St. & 2nd Ave")
            InfoRow(Icons.Outlined.Layers,     "Strokes",    "47 strokes, 3 layers")
            InfoRow(Icons.Outlined.Lock,       "Visibility", "Public")
        }
    }
}

@Composable
private fun InfoRow(
    icon  : androidx.compose.ui.graphics.vector.ImageVector,
    label : String,
    value : String,
) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ColorSurface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, label, tint = ColorOnSurfaceMuted, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = ColorOnSurfaceMuted)
            Text(value, style = MaterialTheme.typography.bodyMedium,  color = ColorOnSurface)
        }
    }
}
