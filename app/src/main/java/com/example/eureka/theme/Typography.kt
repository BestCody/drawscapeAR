package com.example.eureka.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SpaceMono = FontFamily.Monospace
val DMSans    = FontFamily.SansSerif

val ARDrawTypography = Typography(
    displayLarge = TextStyle(
        fontFamily    = SpaceMono,
        fontWeight    = FontWeight.Bold,
        fontSize      = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily    = SpaceMono,
        fontWeight    = FontWeight.Medium,
        fontSize      = 20.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DMSans,
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = SpaceMono,
        fontWeight    = FontWeight.Normal,
        fontSize      = 11.sp,
        letterSpacing = 0.4.sp
    )
)
