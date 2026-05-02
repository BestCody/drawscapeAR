package com.example.eureka.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.example.eureka.ui.screens.home.DrawingPreview
import com.example.eureka.ui.theme.*

@Composable
fun NearbyDrawingCard(
    drawing  : DrawingPreview,
    modifier : Modifier = Modifier,
    onClick  : (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ColorSurface)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ColorPrimary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Layers, "layers", tint = ColorPrimary, modifier = Modifier.size(22.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(drawing.title,  style = MaterialTheme.typography.titleMedium, color = ColorOnSurface)
            Text(drawing.author, style = MaterialTheme.typography.bodyMedium,  color = ColorOnSurfaceMuted)
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(ColorSurfaceVariant)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Outlined.LocationOn, null, tint = ColorOnSurfaceMuted, modifier = Modifier.size(12.dp))
            Text(drawing.distance, style = MaterialTheme.typography.labelSmall, color = ColorOnSurfaceMuted)
        }
    }
}

@Composable
fun PulsingFab(onClick: () -> Unit, label: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue  = 1f,
        targetValue   = 1.08f,
        animationSpec = infiniteRepeatable(
            animation  = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabScale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(ColorPrimary.copy(alpha = 0.18f))
        )
        FloatingActionButton(
            onClick        = onClick,
            shape          = CircleShape,
            containerColor = ColorPrimary,
            contentColor   = ColorBackground,
            modifier       = Modifier.size(56.dp)
        ) {
            Icon(Icons.Filled.Add, label, modifier = Modifier.size(24.dp))
        }
    }
}
