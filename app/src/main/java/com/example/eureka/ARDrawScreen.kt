package com.example.eureka

import android.view.View
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.eureka.theme.ColorAccent
import com.example.eureka.theme.ColorOnSurface
import com.example.eureka.theme.ColorOnSurfaceMuted
import com.example.eureka.theme.ColorPrimary
import com.example.eureka.theme.ColorStrokeDefault
import com.example.eureka.theme.ColorStrokeGold
import com.example.eureka.theme.ColorStrokeGreen
import com.example.eureka.theme.ColorStrokePurple
import com.example.eureka.theme.ColorStrokeWhite
import com.example.eureka.theme.ColorSurface

enum class DrawingTool { BRUSH, ERASER, SELECT }

data class ARDrawUIState(
    val activeTool      : DrawingTool = DrawingTool.BRUSH,
    val strokeColor     : Color       = ColorStrokeDefault,
    val strokeWidth     : Float       = 8f,
    val isAnchored      : Boolean     = false,
    val isSaving        : Boolean     = false,
    val showColorPicker : Boolean     = false,
    val undoStack       : Int         = 0,
    val redoStack       : Int         = 0,
)

@Composable
fun ARDrawScreen() {
    var uiState by remember { mutableStateOf(ARDrawUIState()) }

    Box(modifier = Modifier.fillMaxSize()) {

        // AR surface — replace AndroidView content with ArSceneView when ready
        AndroidView(
            factory  = { ctx ->
                View(ctx).apply {
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        AnchorStatusBanner(
            isAnchored = uiState.isAnchored,
            modifier   = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp)
        )

        AnimatedVisibility(
            visible  = uiState.showColorPicker,
            enter    = fadeIn() + slideInVertically { it },
            exit     = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp)
        ) {
            ColorPickerRow(
                selectedColor = uiState.strokeColor,
                onColorPicked = { color ->
                    uiState = uiState.copy(strokeColor = color, showColorPicker = false)
                }
            )
        }

        ARBottomToolbar(
            uiState             = uiState,
            onToggleColorPicker = { uiState = uiState.copy(showColorPicker = !uiState.showColorPicker) },
            onSelectTool        = { tool -> uiState = uiState.copy(activeTool = tool) },
            onSave              = { },
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

@Composable
private fun ARTopBar(
    canUndo  : Boolean,
    canRedo  : Boolean,
    onUndo   : () -> Unit,
    onRedo   : () -> Unit,
    modifier : Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            GlassIconButton(onClick = onUndo, enabled = canUndo) {
                Icon(Icons.Outlined.Undo, "Undo", tint = if (canUndo) ColorOnSurface else ColorOnSurfaceMuted)
            }
            GlassIconButton(onClick = onRedo, enabled = canRedo) {
                Icon(Icons.Outlined.Redo, "Redo", tint = if (canRedo) ColorOnSurface else ColorOnSurfaceMuted)
            }
        }
    }
}

@Composable
private fun AnchorStatusBanner(isAnchored: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue  = 0.5f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse),
        label         = "bannerAlpha"
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isAnchored) ColorPrimary.copy(alpha = 0.18f) else ColorAccent.copy(alpha = 0.18f))
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        (if (isAnchored) ColorPrimary else ColorAccent)
                            .copy(alpha = if (isAnchored) 1f else alpha)
                    )
            )
            Text(
                text  = if (isAnchored) "Anchored to space" else "Scanning environment…",
                style = MaterialTheme.typography.labelSmall,
                color = if (isAnchored) ColorPrimary else ColorOnSurfaceMuted
            )
        }
    }
}

@Composable
private fun ARBottomToolbar(
    uiState             : ARDrawUIState,
    onToggleColorPicker : () -> Unit,
    onSelectTool        : (DrawingTool) -> Unit,
    onSave              : () -> Unit,
    modifier            : Modifier = Modifier,
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(ColorSurface.copy(alpha = 0.85f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(uiState.strokeColor)
                .border(2.dp, ColorOnSurfaceMuted.copy(alpha = 0.4f), CircleShape)
                .clickable(onClick = onToggleColorPicker)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ToolButton(Icons.Outlined.Edit,"Brush",uiState.activeTool == DrawingTool.BRUSH)   { onSelectTool(DrawingTool.BRUSH) }
            ToolButton(Icons.Outlined.AutoFixHigh, "Erase", uiState.activeTool == DrawingTool.ERASER) { onSelectTool(DrawingTool.ERASER) }
            ToolButton(Icons.Outlined.SelectAll, "Select", uiState.activeTool == DrawingTool.SELECT)  { onSelectTool(DrawingTool.SELECT) }
        }

        FilledTonalButton(
            onClick = onSave,
            shape   = RoundedCornerShape(20.dp),
            colors  = ButtonDefaults.filledTonalButtonColors(
                containerColor = ColorPrimary.copy(alpha = 0.18f),
                contentColor   = ColorPrimary
            )
        ) {
            Icon(Icons.Outlined.CloudUpload, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Save", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun ColorPickerRow(selectedColor: Color, onColorPicked: (Color) -> Unit) {
    val palette = listOf(
        ColorStrokeDefault, ColorStrokeWhite, ColorStrokeGold,
        ColorStrokePurple, ColorStrokeGreen, ColorAccent
    )
    Row(
        modifier              = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(ColorSurface.copy(alpha = 0.9f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        palette.forEach { color ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(if (isSelected) 36.dp else 28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(if (isSelected) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                    .clickable { onColorPicked(color) }
            )
        }
    }
}

@Composable
private fun GlassIconButton(onClick: () -> Unit, enabled: Boolean = true, content: @Composable () -> Unit) {
    Box(
        modifier         = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(ColorSurface.copy(alpha = 0.7f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun ToolButton(
    icon     : ImageVector,
    label    : String,
    selected : Boolean,
    onClick  : () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) ColorPrimary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = label,
            tint               = Color.Black,
            modifier           = Modifier.size(20.dp)
        )
    }
}
