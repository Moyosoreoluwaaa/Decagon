package com.octane.wallet.presentation.utils

import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Applies the signature "Metallic" White-Black-White gradient border.
 * * @param angleDeg The angle of the light source/gradient.
 * - 170f: Steep Diagonal (Cards)
 * - 135f: Standard Diagonal (Buttons)
 * - 90f: Horizontal (List Rows)
 * - 180f: Vertical (Pills/Badges)
 */
fun Modifier.metallicBorder(
    width: Dp,
    shape: androidx.compose.ui.graphics.Shape,
    angleDeg: Float
): Modifier {
    return this.border(
        width = width,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White,
                Color.Black,
                Color.White,
                Color.Black
            ),
            // simple angle calculation for linear gradient start/end
            start = Offset.Zero,
            end = calculateGradientEnd(angleDeg)
        ),
        shape = shape
    )
}

// Helper to approximate gradient direction based on angle (0 is Left->Right)
private fun calculateGradientEnd(angleDeg: Float): Offset {
    val rad = Math.toRadians(angleDeg.toDouble())
    // This is a simplified infinite offset for the brush to span the content
    // In a real production app, usage of Modifier.drawWithCache is preferred to get exact size
    val scale = 1000f
    return Offset(
        x = (cos(rad) * scale).toFloat(),
        y = (sin(rad) * scale).toFloat()
    )
}