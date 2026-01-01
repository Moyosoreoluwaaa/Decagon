package com.decagon.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun EnhancedPriceChart(
    prices: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary
) {
    val maxPrice = remember(prices) { prices.maxOrNull() ?: 0.0 }
    val minPrice = remember(prices) { prices.minOrNull() ?: 0.0 }
    val priceRange = remember(prices) { (maxPrice - minPrice).coerceAtLeast(0.01) }
    // 1. Extract the color here (inside the Composable context)
    val dotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showTooltip by remember { mutableStateOf(false) }

    val animationProgress by animateFloatAsState(
        1f,
        tween(1500, easing = EaseOutCubic),
        label = "line"
    )

    Canvas(
        modifier = modifier
            .pointerInput(prices) {
                detectDragGestures(
                    onDragStart = { showTooltip = true },
                    onDragEnd = { showTooltip = false; selectedIndex = null },
                    onDragCancel = { showTooltip = false; selectedIndex = null }
                ) { change, _ ->
                    val step = size.width / (prices.size - 1).coerceAtLeast(1)
                    selectedIndex = (change.position.x / step).toInt().coerceIn(0, prices.lastIndex)
                }
            }
            .padding(vertical = 12.dp)
    ) {
        val width = size.width
        val height = size.height
        val step = width / (prices.size - 1).coerceAtLeast(1)

        // Draw Dotted Background
        val dotSpacing = 10.dp.toPx()
        val horizontalDots = (width / dotSpacing).toInt()
        val verticalDots = (height / dotSpacing).toInt()

        for (i in 0..horizontalDots) {
            for (j in 0..verticalDots) {
                drawCircle(
                    color = dotColor,
                    radius = 1.dp.toPx(),
                    center = Offset(i * dotSpacing, j * dotSpacing)
                )
            }
        }

        if (prices.isEmpty()) return@Canvas

        // 2. Paths
        val linePath = Path()
        val gradientPath = Path()
        val animatedSize = (prices.size * animationProgress).toInt().coerceAtLeast(1)

        prices.take(animatedSize).forEachIndexed { index, price ->
            val x = index * step
            val y = height - ((price - minPrice) / priceRange * height).toFloat()
            if (index == 0) {
                linePath.moveTo(x, y)
                gradientPath.moveTo(x, height)
                gradientPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                gradientPath.lineTo(x, y)
            }
        }
        gradientPath.lineTo((animatedSize - 1) * step, height)
        gradientPath.close()

        // 3. Draw Content
        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.15f), Color.Transparent),
                startY = 0f, endY = height
            )
        )
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 4. Interactive Tooltip & Point
        selectedIndex?.let { index ->
            val x = index * step
            val y = height - ((prices[index] - minPrice) / priceRange * height).toFloat()

            // Vertical Marker Line
            drawLine(
                color = lineColor.copy(alpha = 0.4f),
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1.dp.toPx()
            )

            drawCircle(lineColor, 6.dp.toPx(), Offset(x, y))

            if (showTooltip) {
                drawContext.canvas.nativeCanvas.apply {
                    val priceText = "$${String.format(Locale.US, "%.2f", prices[index])}"
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 34f
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    val textBounds = android.graphics.Rect()
                    textPaint.getTextBounds(priceText, 0, priceText.length, textBounds)

                    val pad = 12.dp.toPx()
                    val rw = textBounds.width() + pad * 2
                    val rh = textBounds.height() + pad * 2
                    val rx = (x - rw / 2).coerceIn(0f, width - rw)
                    val ry = (y - rh - 20.dp.toPx()).coerceAtLeast(0f)

                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.8f),
                        topLeft = Offset(rx, ry),
                        size = Size(rw, rh),
                        cornerRadius = CornerRadius(6.dp.toPx())
                    )
                    drawText(priceText, rx + pad, ry + rh - pad, textPaint)
                }
            }
        }
    }
}
