package com.example.eureka

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.google.ar.core.exceptions.NotYetAvailableException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.sqrt

private const val TAG = "ARDrawViewModel"

fun distance(a: FloatArray, b: FloatArray): Float {
    val dx = a[0] - b[0]; val dy = a[1] - b[1]; val dz = a[2] - b[2]
    return sqrt(dx * dx + dy * dy + dz * dz)
}

sealed class DrawingState {
    object Idle : DrawingState()
    data class Drawing(
        val currentStrokeId: String,
        val points: MutableList<FloatArray>
    ) : DrawingState()
}

@HiltViewModel
class ARDrawViewModel @Inject constructor(
    private val strokeRepository: StrokeRepository
) : ViewModel() {

    private var handTracker: HandTracker? = null

    // CloudAnchorManager is now initialised via initCloudAnchor(session) from the Composable,
    // once ARSceneView has created its session. Previously it was always null.
    private var cloudAnchorManager: CloudAnchorManager? = null
    private var strokeRenderer: StrokeRenderer? = null

    val uiState      = mutableStateOf(ARDrawUIState())
    val drawingState = mutableStateOf<DrawingState>(DrawingState.Idle)
    val sceneNodes   = MutableStateFlow<List<() -> Unit>>(emptyList())

    private var spaceId: String = ""
    private var anchorPose: Pose? = null
    private var userId: String = ""
    private var latestFrame: Frame? = null

    // Undo/redo stacks — list of complete snapshots of saved stroke IDs
    private val undoStack = ArrayDeque<Stroke>()
    private val redoStack = ArrayDeque<Stroke>()

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    fun initialize(context: Context) {
        handTracker = HandTracker(context).also { tracker ->
            tracker.onResult = { fingerPoint -> handleFingerPoint(fingerPoint) }
        }
        viewModelScope.launch {
            userId = ensureSignedIn()
        }
    }

    /**
     * Called from ARDrawScreen once the ARCore Session is available.
     * Previously CloudAnchorManager was never instantiated, so hosting/resolving never ran.
     */
    fun initCloudAnchor(session: Session) {
        cloudAnchorManager = CloudAnchorManager(session).apply {
            onAnchorHosted = { cloudAnchorId ->
                viewModelScope.launch { handleAnchorHosted(cloudAnchorId) }
            }
            onError = { msg -> Log.e(TAG, "Cloud anchor error: $msg") }
        }
    }

    fun setAnchorData(pose: Pose, newSpaceId: String) {
        anchorPose = pose
        spaceId    = newSpaceId
        uiState.value = uiState.value.copy(isAnchored = true)
        startObservingStrokes()
    }

    fun setStrokeRenderer(renderer: StrokeRenderer) {
        strokeRenderer = renderer
    }

    // ── Frame updates ─────────────────────────────────────────────────────────

    fun Frame.tryAcquireCameraImage() = try {
        acquireCameraImage()
    } catch (e: NotYetAvailableException) {
        null
    } catch (e: RuntimeException) {
        // DeadlineExceededException, ResourceExhaustedException, etc.
        Log.e(TAG, "Failed to acquire camera image", e)
        null
    }

    fun onFrameUpdated(frame: Frame) {
        latestFrame = frame

        if (frame.camera.trackingState == TrackingState.TRACKING) {
            try {
                frame.tryAcquireCameraImage()?.use { image ->
                    handTracker?.processFrame(
                        image.toBitmap(),
                        frame.timestamp / 1_000_000
                    )
                }
            } catch (e: NotYetAvailableException) {
                // Image not ready this frame — safe to skip
            }
        }

        cloudAnchorManager?.checkHostingStatus()
        cloudAnchorManager?.checkResolvingStatus()
    }

    // ── UI event handlers (wired up from ARDrawScreen) ────────────────────────

    fun toggleColorPicker() {
        uiState.value = uiState.value.copy(showColorPicker = !uiState.value.showColorPicker)
    }

    fun selectTool(tool: DrawingTool) {
        uiState.value = uiState.value.copy(activeTool = tool)
    }

    fun setStrokeColor(color: Color) {
        uiState.value = uiState.value.copy(strokeColor = color, showColorPicker = false)
    }

    fun setPublic(isPublic: Boolean) {
        uiState.value = uiState.value.copy(isPublicDrawing = isPublic)
    }

    fun saveCurrentDrawing() {
        // Saving is already handled inside finalizeStroke() on finger lift.
        // This can trigger an explicit manual save if needed in the future.
        Log.d(TAG, "Manual save requested — strokes are auto-saved on stroke completion.")
    }

    // ── Undo / Redo ───────────────────────────────────────────────────────────

    fun undo() {
        val stroke = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(stroke)
        uiState.value = uiState.value.copy(
            undoStack = undoStack.size,
            redoStack = redoStack.size
        )
        viewModelScope.launch {
            strokeRepository.deleteStroke(spaceId, stroke.id)
        }
    }

    fun redo() {
        val stroke = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(stroke)
        uiState.value = uiState.value.copy(
            undoStack = undoStack.size,
            redoStack = redoStack.size
        )
        viewModelScope.launch {
            strokeRepository.saveStroke(spaceId, stroke)
        }
    }

    // ── Internal drawing logic ────────────────────────────────────────────────

    private fun handleFingerPoint(fingerPoint: FingerPoint) {
        if (!fingerPoint.isTracking) {
            if (drawingState.value is DrawingState.Drawing) finalizeStroke()
            return
        }
        val frame = latestFrame ?: return
        val pose  = anchorPose  ?: return

        val worldPoint = projectFingerToWorldAtDepth(frame, fingerPoint.x, fingerPoint.y)
        val localPoint = worldToAnchorRelative(worldPoint, pose)
        onFingerMoved(localPoint)
    }

    private fun onFingerMoved(localPoint: FloatArray) {
        when (val state = drawingState.value) {
            is DrawingState.Idle -> {
                drawingState.value = DrawingState.Drawing(
                    currentStrokeId = UUID.randomUUID().toString(),
                    points          = mutableListOf(localPoint)
                )
            }
            is DrawingState.Drawing -> {
                val last = state.points.lastOrNull()
                if (last == null || distance(last, localPoint) > 0.01f) {
                    val updatedState = state.copy(
                        points = (state.points + localPoint).toMutableList()
                    )
                    drawingState.value = updatedState
                    updateLiveStrokeNodes(updatedState)
                }
            }
        }
    }

    private fun updateLiveStrokeNodes(state: DrawingState.Drawing) {
        sceneNodes.value = emptyList() // Replace with actual Filament node updates
    }

    private fun finalizeStroke() {
        val state = drawingState.value as? DrawingState.Drawing ?: return
        drawingState.value = DrawingState.Idle

        if (state.points.size < 2 || spaceId.isBlank() || userId.isBlank()) return

        val stroke = Stroke(
            id       = state.currentStrokeId,
            points   = state.points.toList(),
            color    = uiState.value.strokeColor,
            width    = uiState.value.strokeWidth,
            authorId = userId,
            isPublic = uiState.value.isPublicDrawing
        )

        undoStack.addLast(stroke)
        redoStack.clear()
        uiState.value = uiState.value.copy(
            undoStack = undoStack.size,
            redoStack = 0
        )

        viewModelScope.launch {
            strokeRepository.saveStroke(spaceId, stroke)
        }
    }

    private suspend fun handleAnchorHosted(cloudAnchorId: String) {
        if (userId.isBlank()) return
        val newSpaceId = strokeRepository.createSpace(cloudAnchorId, userId)
        setAnchorData(anchorPose ?: return, newSpaceId)
    }

    fun startObservingStrokes() {
        if (spaceId.isBlank() || userId.isBlank()) return
        viewModelScope.launch {
            strokeRepository.observeStrokes(spaceId, userId).collect { strokes ->
                strokes.forEach { stroke -> strokeRenderer?.renderStroke(stroke) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        handTracker?.close()
    }
}