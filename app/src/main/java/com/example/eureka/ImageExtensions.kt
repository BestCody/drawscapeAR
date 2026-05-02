package com.example.eureka

import android.graphics.ImageFormat
import android.graphics.YuvImage
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.media.Image

fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out      = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 75, out)
    return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
}