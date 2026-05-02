package com.example.eureka.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eureka.ui.components.NearbyDrawingCard
import com.example.eureka.ui.components.PulsingFab
import com.example.eureka.ui.theme.*

data class DrawingPreview(
    val id         : String,
    val title      : String,
    val author     : String,
    val distance   : String,
    val layerCount : Int
)

@Composable
fun HomeScreen(
    onStartDrawing : () -> Unit,
    onOpenDiscover : () -> Unit,
    onOpenProfile  : () -> Unit,
) {
    val nearbyDrawings = remember {
        listOf(
            DrawingPreview("1", "Street Mural #47",  "@kai",  "12m away",  3),
            DrawingPreview("2", "Floating Garden",   "@mira", "38m away",  7),
            DrawingPreview("3", "Time Capsule 2024", "@drew", "102m away", 1),
        )
    }

    Scaffold(
        containerColor = ColorBackground,
        floatingActionButton = {
            PulsingFab(onClick = onStartDrawing, label = "Draw")
        }
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                HomeHeader(
                    onOpenDiscover = onOpenDiscover,
                    onOpenProfile  = onOpenProfile
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(ColorPrimary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "NEARBY DRAWINGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ColorOnSurfaceMuted
                    )
                }
            }

            items(nearbyDrawings) { drawing ->
                NearbyDrawingCard(
                    drawing  = drawing,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HomeHeader(
    onOpenDiscover : () -> Unit,
    onOpenProfile  : () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(ColorPrimary.copy(alpha = 0.15f), Color.Transparent),
                        center = Offset(size.width * 0.2f, 0f),
                        radius = size.width * 0.8f
                    )
                )
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text  = "EUREKA",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = ColorPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onOpenDiscover) {
                    Icon(Icons.Outlined.Explore, "Discover", tint = ColorOnSurface)
                }
                IconButton(onClick = onOpenProfile) {
                    Icon(Icons.Outlined.AccountCircle, "Profile", tint = ColorOnSurface)
                }
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text       = "Leave your mark\non the world.",
                style      = MaterialTheme.typography.displayLarge,
                color      = ColorOnSurface,
                lineHeight = 38.sp
            )
        }
    }
}
