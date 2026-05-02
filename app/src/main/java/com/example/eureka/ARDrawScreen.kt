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
import com.example.eureka.theme.*

// Define all drawing tools
enum class DrawingTool { BRUSH, ERASER, SELECT }


data class ARDrawUIState(
    val activeTool      : DrawingTool = DrawingTool.BRUSH,
    val strokeColor     : Color       = ColorStrokeDefault,
    val strokeWidth     : Float       = 8f,
    val isAnchored      : Boolean     = false, // Anchor the drawing to space
    val isSaving        : Boolean     = false,
    val showColorPicker : Boolean     = false,
    val undoStack       : Int         = 0,
    val redoStack       : Int         = 0,
)

// Maps each tool to its theme color and icon
private val toolConfig = mapOf(
    DrawingTool.BRUSH  to Pair(ColorBrush,  Icons.Outlined.Edit),
    DrawingTool.ERASER to Pair(ColorEraser, Icons.Outlined.AutoFixHigh),
    DrawingTool.SELECT to Pair(ColorFiller, Icons.Outlined.SelectAll),
)

@Composable
// To draw the AR camera window
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

        // Top bar
        ARTopBar(
            canUndo  = uiState.undoStack > 0,
            canRedo  = uiState.redoStack > 0,
            onUndo   = { uiState = uiState.copy(undoStack = (uiState.undoStack - 1).coerceAtLeast(0)) },
            onRedo   = { uiState = uiState.copy(redoStack = (uiState.redoStack - 1).coerceAtLeast(0)) },
            onExit   = { /* TODO: wire up navigation callback */ },
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
        )

        // Anchor status banner
        AnchorStatusBanner(
            isAnchored = uiState.isAnchored,
            modifier   = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp)
        )

        // Color picker (shown on demand)
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

        // Bottom toolbar
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

// Top bar
@Composable
private fun ARTopBar(
    canUndo  : Boolean,
    canRedo  : Boolean,
    onUndo   : () -> Unit,
    onRedo   : () -> Unit,
    onExit   : () -> Unit,
    modifier : Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Close button on the left
        GlassIconButton(onClick = onExit) {
            Icon(
                imageVector        = Icons.Filled.Close,
                contentDescription = "Exit",
                tint               = Color.White
            )
        }

        // Undo / Redo on the right
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            GlassIconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    imageVector        = Icons.Outlined.Undo,
                    contentDescription = "Undo",
                    // White when available, visibly dimmed when not
                    tint               = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
            GlassIconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    imageVector        = Icons.Outlined.Redo,
                    contentDescription = "Redo",
                    tint               = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// Anchor status banner

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
            .background(
                if (isAnchored) ColorPrimary.copy(alpha = 0.18f)
                else ColorAccent.copy(alpha = 0.18f)
            )
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
                color = Color.White
            )
        }
    }
}

// ── Bottom toolbar ────────────────────────────────────────────────────────────

@Composable
private fun ARBottomToolbar(
    uiState             : ARDrawUIState,
    onToggleColorPicker : () -> Unit,
    onSelectTool        : (DrawingTool) -> Unit,
    onSave              : () -> Unit,
    modifier            : Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 1f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        // Stroke color swatch
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(uiState.strokeColor)
                .border(2.dp, Color.Black.copy(alpha = 1f), CircleShape)
                .clickable(onClick = onToggleColorPicker)
        )

        // Tool buttons — each lights up in its own theme color when active
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            toolConfig.forEach { (tool, config) ->
                val (toolColor, toolIcon) = config
                ToolButton(
                    icon     = toolIcon,
                    label    = tool.name,
                    selected = uiState.activeTool == tool,
                    // The active background tint uses that tool's specific color
                    activeColor = toolColor,
                    onClick  = { onSelectTool(tool) }
                )
            }
        }

        // Save button
        FilledTonalButton(
            onClick = onSave,
            shape   = RoundedCornerShape(20.dp),
            colors  = ButtonDefaults.filledTonalButtonColors(
                containerColor = ColorCloud.copy(alpha = 0.25f),
                contentColor   = ColorCloud
            )
        ) {
            Icon(Icons.Outlined.CloudUpload, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Save", style = MaterialTheme.typography.labelSmall)
        }
    }
}

// Color picker strip

@Composable
private fun ColorPickerRow(selectedColor: Color, onColorPicked: (Color) -> Unit) {
    val palette = listOf(
        ColorStrokeDefault, ColorStrokeWhite, ColorStrokeGold,
        ColorStrokePurple, ColorStrokeGreen, ColorAccent
    )
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.Black.copy(alpha = 0.7f))
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
                    .then(
                        if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                        else Modifier
                    )
                    .clickable { onColorPicked(color) }
            )
        }
    }
}

// Glass icon button

@Composable
private fun GlassIconButton(
    onClick  : () -> Unit,
    enabled  : Boolean = true,
    content  : @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

// Tool button

@Composable
private fun ToolButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (selected) activeColor else Color.Transparent
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
    }
}