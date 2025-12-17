package com.decagon.wallet.presentation.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    lineColor: Color = Color.Black,
    gradientColors: List<Color> = listOf(
        Color.Blue.copy(alpha = 0.3f),
        Color.Blue.copy(alpha = 0.1f)
    )
) {
    val maxPrice = remember(prices) { prices.maxOrNull() ?: 0.0 }
    val minPrice = remember(prices) { prices.minOrNull() ?: 0.0 }
    val priceRange = remember(prices) { maxPrice - minPrice }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showTooltip by remember { mutableStateOf(false) }

    // Animations
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2000, easing = EaseOutCubic),
        label = "line_animation"
    )

    val gradientProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2500, delayMillis = 500, easing = EaseOutCubic),
        label = "gradient_animation"
    )

    val pointerModifier = Modifier.pointerInput(prices) {
        detectTapGestures(
            onTap = { offset ->
                val width = size.width
                val step = width / (prices.size - 1).coerceAtLeast(1)
                val index = (offset.x / step).toInt().coerceIn(0, prices.lastIndex)
                selectedIndex = index
                showTooltip = true
            }
        )
        detectDragGestures(
            onDragStart = { position ->
                showTooltip = true
                val width = size.width
                val step = width / (prices.size - 1).coerceAtLeast(1)
                val index = (position.x / step).toInt().coerceIn(0, prices.lastIndex)
                selectedIndex = index
            },
            onDragEnd = {
                showTooltip = false
                selectedIndex = null
            },
            onDragCancel = {
                showTooltip = false
                selectedIndex = null
            }
        ) { change, _ ->
            val width = size.width
            val step = width / (prices.size - 1).coerceAtLeast(1)
            val xPosition = change.position.x
            val index = (xPosition / step).toInt().coerceIn(0, prices.lastIndex)
            selectedIndex = index
        }
    }

    Canvas(
        modifier = modifier
            .then(pointerModifier)
            .padding(vertical = 8.dp)
    ) {
        if (prices.isEmpty() || priceRange == 0.0) return@Canvas

        val width = size.width
        val height = size.height
        val step = width / (prices.size - 1).coerceAtLeast(1)

        // Line path
        val linePath = androidx.compose.ui.graphics.Path().apply {
            val animatedSize = (prices.size * animationProgress).toInt().coerceAtLeast(1)
            prices.take(animatedSize).forEachIndexed { index, price ->
                val x = index * step
                val y = height - ((price - minPrice) / priceRange * height).toFloat()

                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }

        // Gradient path
        val gradientPath = androidx.compose.ui.graphics.Path().apply {
            val animatedSize = (prices.size * gradientProgress).toInt().coerceAtLeast(1)

            moveTo(0f, height)

            prices.take(animatedSize).forEachIndexed { index, price ->
                val x = index * step
                val y = height - ((price - minPrice) / priceRange * height).toFloat()
                lineTo(x, y)
            }

            if (animatedSize > 0) {
                lineTo((animatedSize - 1) * step, height)
            }
            close()
        }

        // Draw gradient
        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = gradientColors,
                startY = 0f,
                endY = height
            )
        )

        // Draw grid lines
        val gridColor = Color.Gray.copy(alpha = 0.2f)
        for (i in 1..4) {
            val y = height * i / 5
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Draw line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw glow
        drawPath(
            path = linePath,
            color = lineColor.copy(alpha = 0.3f),
            style = Stroke(
                width = 8.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw selected point
        selectedIndex?.let { index ->
            if (index in prices.indices) {
                val x = index * step
                val y = height - ((prices[index] - minPrice) / priceRange * height).toFloat()

                drawCircle(
                    color = lineColor.copy(alpha = 0.3f),
                    radius = 16.dp.toPx(),
                    center = Offset(x, y)
                )

                drawCircle(
                    color = lineColor,
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )

                // Tooltip
                if (showTooltip) {
                    drawContext.canvas.nativeCanvas.apply {
                        val priceText = "$${String.format(Locale.US, "%.4f", prices[index])}"
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
                        }

                        val textBounds = android.graphics.Rect()
                        textPaint.getTextBounds(priceText, 0, priceText.length, textBounds)

                        val padding = 12.dp.toPx()
                        val tooltipWidth = textBounds.width() + padding * 2
                        val tooltipHeight = textBounds.height() + padding * 2

                        val tooltipX = (x - tooltipWidth / 2).coerceIn(0f, width - tooltipWidth)
                        val tooltipY = (y - tooltipHeight - 16.dp.toPx()).coerceAtLeast(0f)

                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.8f),
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipWidth, tooltipHeight),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )

                        drawText(
                            priceText,
                            tooltipX + tooltipWidth / 2,
                            tooltipY + tooltipHeight / 2 + textBounds.height() / 2,
                            textPaint
                        )
                    }
                }
            }
        }

        // Min/max indicators
        if (prices.isNotEmpty()) {
            val maxIndex = prices.indexOfFirst { it == maxPrice }
            val minIndex = prices.indexOfFirst { it == minPrice }

            val maxX = maxIndex * step
            val maxY = height - ((maxPrice - minPrice) / priceRange * height).toFloat()
            drawCircle(
                color = Color.Green.copy(alpha = 0.7f),
                radius = 6.dp.toPx(),
                center = Offset(maxX, maxY)
            )

            val minX = minIndex * step
            val minY = height - ((minPrice - minPrice) / priceRange * height).toFloat()
            drawCircle(
                color = Color.Red.copy(alpha = 0.7f),
                radius = 6.dp.toPx(),
                center = Offset(minX, minY)
            )
        }
    }
}