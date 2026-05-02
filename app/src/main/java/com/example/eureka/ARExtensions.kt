package com.example.eureka
import com.google.ar.core.Frame
import com.google.ar.core.Pose

fun projectFingerToWorldAtDepth(
    frame: Frame,
    fingerX: Float,
    fingerY: Float,
    depthMeters: Float = 0.5f  // 50cm in front of camera
): FloatArray {
    val cameraPose = frame.camera.pose
    val forward    = floatArrayOf(
        -cameraPose.zAxis[0], -cameraPose.zAxis[1], -cameraPose.zAxis[2]
    )
    val camPos = cameraPose.translation

    val worldX = camPos[0] + forward[0] * depthMeters + (fingerX - 0.5f) * depthMeters
    val worldY = camPos[1] + forward[1] * depthMeters - (fingerY - 0.5f) * depthMeters
    val worldZ = camPos[2] + forward[2] * depthMeters

    return floatArrayOf(worldX, worldY, worldZ)
}

fun worldToAnchorRelative(worldPos: FloatArray, anchorPose: Pose): FloatArray {
    val worldPose = Pose.makeTranslation(worldPos[0], worldPos[1], worldPos[2])
    val localPose = anchorPose.inverse().compose(worldPose)
    return localPose.translation
}

fun anchorRelativeToWorld(localPos: FloatArray, anchorPose: Pose): FloatArray {
    val localPose = Pose.makeTranslation(localPos[0], localPos[1], localPos[2])
    val worldPose = anchorPose.compose(localPose)
    return worldPose.translation
}