package com.example.eureka

import android.Manifest
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.eureka.theme.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.ar.core.Config
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.rememberEngine   // required in SceneView 4.x

// ── Enums / data ──────────────────────────────────────────────────────────────

enum class DrawingTool { BRUSH, ERASER, SELECT }

data class ARDrawUIState(
    val activeTool      : DrawingTool = DrawingTool.BRUSH,
    val strokeColor     : Color       = ColorStrokeDefault,
    val strokeWidth     : Float       = 8f,
    val isAnchored      : Boolean     = false,
    val isSaving        : Boolean     = false,
    val showColorPicker : Boolean     = false,
    val isPublicDrawing : Boolean     = true,
    val undoStack       : Int         = 0,
    val redoStack       : Int         = 0,
)

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
fun ARTopBar(
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
        GlassIconButton(onClick = onExit) {
            Icon(Icons.Filled.Close, contentDescription = "Exit", tint = Color.White)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            GlassIconButton(onClick = onUndo, enabled = canUndo) {
                Icon(
                    Icons.Outlined.Undo, contentDescription = "Undo",
                    tint = if (canUndo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
            GlassIconButton(onClick = onRedo, enabled = canRedo) {
                Icon(
                    Icons.Outlined.Redo, contentDescription = "Redo",
                    tint = if (canRedo) Color.White else Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}

// ── Anchor banner ─────────────────────────────────────────────────────────────

@Composable
fun AnchorStatusBanner(isAnchored: Boolean, modifier: Modifier = Modifier) {
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
fun ARBottomToolbar(
    uiState            : ARDrawUIState,
    onToggleColorPicker: () -> Unit,
    onSelectTool       : (DrawingTool) -> Unit,
    onTogglePublic     : (Boolean) -> Unit,
    onSave             : () -> Unit,
    onOpenProfile      : () -> Unit,
    modifier           : Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Color swatch — tapping opens color picker
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(uiState.strokeColor)
                    .clickable { onToggleColorPicker() }
            )

            // Tool buttons — ToolButton now receives the required activeColor param (was missing before)
            ToolButton(
                icon        = Icons.Outlined.Edit,
                label       = "Brush",
                selected    = uiState.activeTool == DrawingTool.BRUSH,
                activeColor = ColorBrush,
                onClick     = { onSelectTool(DrawingTool.BRUSH) }
            )

            ToolButton(
                icon        = Icons.Outlined.AutoFixHigh,
                label       = "Eraser",
                selected    = uiState.activeTool == DrawingTool.ERASER,
                activeColor = ColorEraser,
                onClick     = { onSelectTool(DrawingTool.ERASER) }
            )

            IconToggleButton(
                checked         = uiState.isPublicDrawing,
                onCheckedChange = onTogglePublic
            ) {
                Icon(Icons.Outlined.Visibility, contentDescription = "Public toggle")
            }

            Button(onClick = onSave) { Text("Save") }
        }

        BottomModeSwitcher(
            drawSelected  = true,
            onDrawClick   = {},
            onProfileClick = onOpenProfile
        )
    }
}

// ── Color picker ──────────────────────────────────────────────────────────────

@Composable
fun ColorPickerRow(selectedColor: Color, onColorPicked: (Color) -> Unit) {
    val palette = listOf(
        ColorStrokeDefault, ColorStrokeWhite, ColorStrokeGold,
        ColorStrokePurple,  ColorStrokeGreen, ColorAccent
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
                    .then(if (isSelected) Modifier.border(2.dp, Color.White, CircleShape) else Modifier)
                    .clickable { onColorPicked(color) }
            )
        }
    }
}

// ── Shared small components ───────────────────────────────────────────────────

@Composable
fun GlassIconButton(
    onClick : () -> Unit,
    enabled : Boolean = true,
    content : @Composable () -> Unit,
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

/**
 * Tool button — now has all 5 required parameters (activeColor was missing at all call sites before).
 */
@Composable
fun ToolButton(
    icon       : ImageVector,
    label      : String,
    selected   : Boolean,
    activeColor: Color,
    onClick    : () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) activeColor else Color.Transparent)
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

@Composable
fun BottomModeSwitcher(
    drawSelected  : Boolean,
    onDrawClick   : () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(40.dp))
            .background(Color.Black.copy(alpha = 0.85f))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (drawSelected) ColorBrush else Color.White)
                .clickable(onClick = onDrawClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Edit, contentDescription = "Draw",
                tint = if (drawSelected) Color.White else Color.Black
            )
        }

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (!drawSelected) ColorCloud else Color.White)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Person, contentDescription = "Profile",
                tint = if (!drawSelected) Color.White else Color.Black
            )
        }
    }
}

// ── Main AR screen ────────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ARDrawScreen(
    onOpenProfile: () -> Unit,
    viewModel    : ARDrawViewModel = hiltViewModel()
) {
    val uiState  by viewModel.uiState
    val context  = LocalContext.current

    // SceneView 4.x requires the Engine to be provided by the host composable.
    // Previously it was omitted entirely, causing a NullPointerException at runtime.
    val engine = rememberEngine()

    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
        viewModel.initialize(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        ARSceneView(
            modifier   = Modifier.fillMaxSize(),
            engine     = engine,          // required in 4.x
            planeRenderer = true,
            sessionConfiguration = { _, config ->
                config.cloudAnchorMode    = Config.CloudAnchorMode.ENABLED
                config.planeFindingMode   = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                // GeospatialMode removed — requires extra setup; add back if needed
            },
            // Wire up CloudAnchorManager once the session is ready.
            // Previously cloudAnchorManager was always null because this callback was missing.
            onSessionCreated = { session ->
                viewModel.initCloudAnchor(session)
            },
            onSessionUpdated = { _, frame ->
                viewModel.onFrameUpdated(frame)
            }
        )

        // Top bar (undo/redo/exit)
        ARTopBar(
            canUndo  = uiState.undoStack > 0,
            canRedo  = uiState.redoStack > 0,
            onUndo   = { viewModel.undo() },
            onRedo   = { viewModel.redo() },
            onExit   = { /* handled by parent navigator */ },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .zIndex(1f)
        )

        // Anchor status banner
        AnchorStatusBanner(
            isAnchored = uiState.isAnchored,
            modifier   = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 56.dp)
                .zIndex(1f)
        )

        // Color picker (shown on demand)
        AnimatedVisibility(
            visible = uiState.showColorPicker,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 140.dp)
                .zIndex(2f),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit  = fadeOut() + slideOutVertically { it / 2 }
        ) {
            ColorPickerRow(
                selectedColor = uiState.strokeColor,
                onColorPicked = { viewModel.setStrokeColor(it) }
            )
        }

        // Bottom toolbar — all lambdas are now real implementations, not { ... } placeholders
        ARBottomToolbar(
            uiState             = uiState,
            onToggleColorPicker = { viewModel.toggleColorPicker() },
            onSelectTool        = { viewModel.selectTool(it) },
            onTogglePublic      = { viewModel.setPublic(it) },
            onSave              = { viewModel.saveCurrentDrawing() },
            onOpenProfile       = onOpenProfile,
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}