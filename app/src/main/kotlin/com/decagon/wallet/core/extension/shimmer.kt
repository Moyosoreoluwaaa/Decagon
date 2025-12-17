package com.octane.wallet.core.extension

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Shimmer loading effect for skeleton screens.
 */
fun Modifier.shimmer(
    durationMillis: Int = 1000,
    highlightColor: Color = Color.LightGray.copy(alpha = 0.6f),
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.3f)
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    background(
        Brush.linearGradient(
            colors = listOf(
                backgroundColor,
                highlightColor,
                backgroundColor
            ),
            start = Offset(translateAnimation, 0f),
            end = Offset(translateAnimation + 200f, 0f)
        )
    )
}

/**
 * Haptic feedback on click.
 */
fun Modifier.hapticClick(
    feedbackType: HapticFeedbackType = HapticFeedbackType.LongPress,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    clickable {
        haptic.performHapticFeedback(feedbackType)
        onClick()
    }
}

/**
 * Conditional modifier - apply only if condition is true.
 */
fun Modifier.conditionally(
    condition: Boolean,
    modifier: Modifier.() -> Modifier
): Modifier {
    return if (condition) this.modifier() else this
}

/**
 * Pulse animation (for highlighting new items).
 */
fun Modifier.pulse(
    durationMillis: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    this.then(
        Modifier.composed {
            graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
        }
    )
}