package com.example.eureka.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ColorSurface          = Color(0xFFFFFFFF)
val ColorSurfaceVariant   = Color(0xFFFFFFFA)
val ColorPrimary          = Color(0xFF6EE7F7)
val ColorAccent           = Color(0xFFFF6B6B)
val ColorOnSurface        = Color(0xFFE6E1E5)
val ColorOnSurfaceMuted   = Color(0xFF938F99)
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
