package com.example.eureka.ui.screens.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.eureka.ui.components.NearbyDrawingCard
import com.example.eureka.ui.screens.home.DrawingPreview
import com.example.eureka.ui.theme.*

@Composable
fun DiscoverScreen(
    onDrawingClick : (String) -> Unit,
    onBack         : () -> Unit,
) {
    var query by remember { mutableStateOf("") }

    val drawings = remember {
        listOf(
            DrawingPreview("1", "Ghost in the Alley",   "@void_ink", "5m away",   12),
            DrawingPreview("2", "Neon Koi",             "@mira",     "22m away",   4),
            DrawingPreview("3", "Blueprint #3",         "@drew",     "55m away",   8),
            DrawingPreview("4", "Portal Sketch",        "@kai",      "110m away",  2),
            DrawingPreview("5", "Daily Glyph 88",       "@anon",     "200m away",  1),
        )
    }

    Scaffold(
        containerColor = ColorBackground,
        topBar = {
            Column(
                modifier = Modifier
                    .background(ColorBackground)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = ColorOnSurface)
                    }
                    Text(
                        text  = "Discover",
                        style = MaterialTheme.typography.titleLarge,
                        color = ColorOnSurface
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ColorSurfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Search, "Search",
                        tint     = ColorOnSurfaceMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (query.isEmpty()) {
                            Text(
                                "Search drawings near you…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ColorOnSurfaceMuted
                            )
                        }
                        BasicTextField(
                            value         = query,
                            onValueChange = { query = it },
                            singleLine    = true,
                            textStyle     = MaterialTheme.typography.bodyMedium.copy(color = ColorOnSurface),
                            modifier      = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    ) { padding ->
        val filtered = if (query.isBlank()) drawings
        else drawings.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.author.contains(query, ignoreCase = true)
        }

        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { drawing ->
                NearbyDrawingCard(
                    drawing = drawing,
                    onClick = { onDrawingClick(drawing.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
