package com.example.eureka

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.google.mediapipe.framework.image.BitmapImageBuilder

data class FingerPoint(
    val x: Float,        // 0.0 → 1.0 (normalized screen position)
    val y: Float,        // 0.0 → 1.0
    val isTracking: Boolean
)

class HandTracker(context: Context) {

    var onResult: ((FingerPoint) -> Unit)? = null

    private val handLandmarker: HandLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .setDelegate(Delegate.GPU)      // Use CPU on emulator
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setNumHands(1)
            .setMinHandDetectionConfidence(0.5f)
            .setMinTrackingConfidence(0.5f)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ -> handleResult(result) }
            .setErrorListener { error -> error.printStackTrace() }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    fun processFrame(bitmap: Bitmap, timestampMs: Long) {
        val mpImage = BitmapImageBuilder(bitmap).build()
        handLandmarker.detectAsync(mpImage, timestampMs)
    }

    private fun handleResult(result: HandLandmarkerResult) {
        if (result.landmarks().isEmpty()) {
            onResult?.invoke(FingerPoint(0f, 0f, isTracking = false))
            return
        }
        // Landmark 8 = index finger tip
        val indexTip = result.landmarks()[0][8]
        onResult?.invoke(
            FingerPoint(
                x          = indexTip.x(),
                y          = indexTip.y(),
                isTracking = true
            )
        )
    }

    fun close() { handLandmarker.close() }
}