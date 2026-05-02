package com.example.eureka
import android.content.Context
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
import androidx.compose.runtime.mutableStateOf
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
        val currentStrokeId : String,
        val points          : MutableList<FloatArray>
    ) : DrawingState()
}
@HiltViewModel
class ARDrawViewModel @Inject constructor(
    private val strokeRepository: StrokeRepository
) : ViewModel() {

    private var handTracker: HandTracker? = null
    private var cloudAnchorManager: CloudAnchorManager? = null

    val uiState      = mutableStateOf(ARDrawUIState())
    val drawingState = mutableStateOf<DrawingState>(DrawingState.Idle)

    // Nodes to add to the SceneView scene, collected as state
    val sceneNodes = MutableStateFlow<List<() -> Unit>>(emptyList())

    private var spaceId   = ""
    private var anchorPose: Pose? = null
    private var userId    = ""

    fun initialize(context: Context) {
        handTracker = HandTracker(context).also { tracker ->
            tracker.onResult = { fingerPoint -> handleFingerPoint(fingerPoint) }
        }
        viewModelScope.launch { userId = ensureSignedIn() }
    }

    fun onFrameUpdated(frame: Frame) {
        // Feed frame to MediaPipe
        try {
            if (frame.camera.trackingState == TrackingState.TRACKING) {
                frame.acquireCameraImage().use { image ->
                    handTracker?.processFrame(image.toBitmap(), frame.timestamp / 1_000_000)
                }
            }
        } catch (e: Exception) { /* ignore */ }

        // Check cloud anchor status
        cloudAnchorManager?.checkHostingStatus()
        cloudAnchorManager?.checkResolvingStatus()

        latestFrame = frame
    }

    private var latestFrame: Frame? = null

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
                val newId = UUID.randomUUID().toString()
                drawingState.value = DrawingState.Drawing(newId, mutableListOf(localPoint))
            }
            is DrawingState.Drawing -> {
                val last = state.points.lastOrNull()
                if (last == null || distance(last, localPoint) > 0.01f) {
                    state.points.add(localPoint)
                    // Trigger recomposition to update the live stroke
                    updateLiveStrokeNodes(state)
                }
            }
        }
    }

    private var strokeRenderer: StrokeRenderer? = null
    strokeRepository.observeStrokes(spaceId, userId).collect { strokes ->
        val pose = anchorPose ?: return@collect
        strokes.forEach { stroke ->
            strokeRenderer?.renderStroke(stroke)
        }
    }

    private fun updateLiveStrokeNodes(state: DrawingState.Drawing) {
        // Emit updated node list — ARSceneView recomposes and renders them
        // Implementation depends on your node management strategy
    }

    private fun finalizeStroke() {
        val state = drawingState.value as? DrawingState.Drawing ?: return
        drawingState.value = DrawingState.Idle
        if (state.points.size < 2) return

        val stroke = Stroke(
            id       = state.currentStrokeId,
            points   = state.points.toList(),
            color    = uiState.value.strokeColor,
            width    = uiState.value.strokeWidth,
            authorId = userId,
            isPublic = uiState.value.isPublicDrawing
        )

        viewModelScope.launch { strokeRepository.saveStroke(spaceId, stroke) }
    }

    fun startObservingStrokes() {
        viewModelScope.launch {
            strokeRepository.observeStrokes(spaceId, userId).collect { strokes ->
                val pose = anchorPose ?: return@collect
                // Convert anchor-relative → world space, then update sceneNodes state
                // SceneView 4 will recompose and render the updated nodes
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        handTracker?.close()
    }
}