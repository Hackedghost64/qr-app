package com.example.qrapp.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

enum class QrDotShape(val label: String) {
    Square("Classic"),
    Circle("Dots"),
    RoundedSquare("Squircles")
}

enum class QrEyeStyle(val label: String) {
    Square("Square"),
    Rounded("Smooth"),
    Circle("Circular")
}

enum class QrLogoType(val label: String) {
    None("None"),
    Web("Globe"),
    Heart("Heart"),
    Star("Star"),
    Share("Share"),
    Link("Chain")
}

data class QrCustomizationConfig(
    val foregroundColor: Int = Color(0xFF0F172A).toArgb(),
    val backgroundColor: Int = Color(0xFFFFFFFF).toArgb(),
    val eyeColor: Int? = null,
    val dotShape: QrDotShape = QrDotShape.Circle,
    val eyeStyle: QrEyeStyle = QrEyeStyle.Rounded,
    val logoType: QrLogoType = QrLogoType.None,
    val logoBadgeColor: Int = Color(0xFFFFFFFF).toArgb(),
    val exportResolution: Int = 1024
)
