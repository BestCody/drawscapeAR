package com.example.eureka

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class Stroke(
    val id            : String      = UUID.randomUUID().toString(),
    val points        : List<FloatArray>,
    val color         : Color,
    val width         : Float,
    val authorId      : String,
    val isPublic      : Boolean     = true,
    val cloudAnchorId : String?     = null
)