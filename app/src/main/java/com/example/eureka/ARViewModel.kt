package com.example.eureka

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ar.core.Frame
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.sqrt

fun distance(a: FloatArray, b: FloatArray): Float {
    val dx = a[0] - b[0]
    val dy = a[1] - b[1]
    val dz = a[2] - b[2]
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
    private var cloudAnchorManager: CloudAnchorManager? = null
    private var strokeRenderer: StrokeRenderer? = null

    val uiState = mutableStateOf(ARDrawUIState())
    val drawingState = mutableStateOf<DrawingState>(DrawingState.Idle)

    // Scene nodes rendered by ARSceneView
    val sceneNodes = MutableStateFlow<List<() -> Unit>>(emptyList())

    private var spaceId: String = ""
    private var anchorPose: Pose? = null
    private var userId: String = ""
    private var latestFrame: Frame? = null

    fun initialize(context: Context) {
        handTracker = HandTracker(context).also { tracker ->
            tracker.onResult = { fingerPoint ->
                handleFingerPoint(fingerPoint)
            }
        }

        viewModelScope.launch {
            userId = ensureSignedIn()
        }
    }

    fun setAnchorData(
        pose: Pose,
        newSpaceId: String
    ) {
        anchorPose = pose
        spaceId = newSpaceId
        uiState.value = uiState.value.copy(isAnchored = true)

        startObservingStrokes()
    }

    fun setStrokeRenderer(renderer: StrokeRenderer) {
        strokeRenderer = renderer
    }

    fun onFrameUpdated(frame: Frame) {
        latestFrame = frame

        try {
            if (frame.camera.trackingState == TrackingState.TRACKING) {
                frame.acquireCameraImage().use { image ->
                    handTracker?.processFrame(
                        image.toBitmap(),
                        frame.timestamp / 1_000_000
                    )
                }
            }
        } catch (_: Exception) {
        }

        cloudAnchorManager?.checkHostingStatus()
        cloudAnchorManager?.checkResolvingStatus()
    }

    private fun handleFingerPoint(fingerPoint: FingerPoint) {
        if (!fingerPoint.isTracking) {
            if (drawingState.value is DrawingState.Drawing) {
                finalizeStroke()
            }
            return
        }

        val frame = latestFrame ?: return
        val pose = anchorPose ?: return

        val worldPoint = projectFingerToWorldAtDepth(
            frame,
            fingerPoint.x,
            fingerPoint.y
        )

        val localPoint = worldToAnchorRelative(worldPoint, pose)

        onFingerMoved(localPoint)
    }

    private fun onFingerMoved(localPoint: FloatArray) {
        when (val state = drawingState.value) {

            is DrawingState.Idle -> {
                drawingState.value = DrawingState.Drawing(
                    currentStrokeId = UUID.randomUUID().toString(),
                    points = mutableListOf(localPoint)
                )
            }

            is DrawingState.Drawing -> {
                val last = state.points.lastOrNull()

                if (last == null || distance(last, localPoint) > 0.01f) {
                    val updatedPoints =
                        (state.points + listOf(localPoint)).toMutableList()

                    val updatedState = state.copy(points = updatedPoints)

                    drawingState.value = updatedState

                    updateLiveStrokeNodes(updatedState)
                }
            }
        }
    }

    private fun updateLiveStrokeNodes(state: DrawingState.Drawing) {
        // Replace with actual SceneView node generation if desired
        sceneNodes.value = emptyList()
    }

    private fun finalizeStroke() {
        val state = drawingState.value as? DrawingState.Drawing ?: return

        drawingState.value = DrawingState.Idle

        if (state.points.size < 2) return
        if (spaceId.isBlank()) return
        if (userId.isBlank()) return

        val stroke = Stroke(
            id = state.currentStrokeId,
            points = state.points.toList(),
            color = uiState.value.strokeColor,
            width = uiState.value.strokeWidth,
            authorId = userId,
            isPublic = uiState.value.isPublicDrawing
        )

        viewModelScope.launch {
            strokeRepository.saveStroke(spaceId, stroke)
        }
    }

    fun startObservingStrokes() {
        if (spaceId.isBlank() || userId.isBlank()) return

        viewModelScope.launch {
            strokeRepository.observeStrokes(spaceId, userId).collect { strokes ->
                val pose = anchorPose ?: return@collect

                strokes.forEach { stroke ->
                    strokeRenderer?.renderStroke(stroke)
                }

                // Optional: rebuild sceneNodes here if renderer is node-based
            }
        }
    }

    fun undo() {
        // TODO: Implement undo logic
    }

    fun redo() {
        // TODO: Implement redo logic
    }

    override fun onCleared() {
        super.onCleared()
        handTracker?.close()
    }
}