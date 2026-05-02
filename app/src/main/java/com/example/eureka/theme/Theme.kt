package com.example.eureka.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ColorSurface          = Color(0xFF14141C)
val ColorSurfaceVariant   = Color(0xFF1E1E2A)
val ColorPrimary          = Color(0xFF6EE7F7)
val ColorPrimaryVariant   = Color(0xFF3BC8DE)
val ColorAccent           = Color(0xFFFF6B6B)
val ColorOnSurface        = Color(0xFFE8E8F0)
val ColorOnSurfaceMuted   = Color(0xFF7A7A94)
val ColorStrokeDefault    = Color(0xFF6EE7F7)
val ColorStrokeWhite      = Color(0xFFFFFFFF)
val ColorStrokeGold       = Color(0xFFFFD166)
val ColorStrokePurple     = Color(0xFFB388FF)
val ColorStrokeGreen      = Color(0xFF69F0AE)

private val DarkColorScheme = darkColorScheme(
    primary          = ColorPrimary,
    secondary        = ColorStrokeGold,
    onBackground     = ColorOnSurface,
    surface          = ColorSurface,
    onSurface        = ColorOnSurface,
    surfaceVariant   = ColorSurfaceVariant,
    onSurfaceVariant = ColorOnSurfaceMuted,
    error            = ColorAccent,
)

@Composable
fun ARDrawTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = ARDrawTypography,
        content     = content
    )
}
