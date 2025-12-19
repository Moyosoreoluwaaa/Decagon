package com.koin.ui.portfolio

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.koin.data.coin.TimeRange
import com.koin.domain.portfolio.Portfolio
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PortfolioPerformanceChart(
    portfolio: Portfolio,
    selectedTimeRange: TimeRange,
    modifier: Modifier = Modifier,
    lineColor: Color = Color.Blue,
    gradientColors: List<Color> = listOf(
        Color.Blue.copy(alpha = 0.3f),
        Color.Blue.copy(alpha = 0.1f)
    )
) {
    // Generate mock portfolio value history based on current state
    val portfolioHistory = remember(portfolio, selectedTimeRange) {
        generatePortfolioHistory(portfolio, selectedTimeRange)
    }

    val maxValue =
        remember(portfolioHistory) { portfolioHistory.maxOrNull() ?: Portfolio.INITIAL_BALANCE }
    val minValue =
        remember(portfolioHistory) { portfolioHistory.minOrNull() ?: Portfolio.INITIAL_BALANCE }
    val valueRange = remember(portfolioHistory) { maxValue - minValue }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showTooltip by remember { mutableStateOf(false) }

    // Animation for line drawing
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2000, easing = EaseOutCubic),
        label = "line_animation"
    )

    // Animation for gradient
    val gradientProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(2500, delayMillis = 500, easing = EaseOutCubic),
        label = "gradient_animation"
    )

    // Determine line color based on performance
    val performanceLineColor = if (portfolio.isPositivePerformance) Color.Green else Color.Red
    val performanceGradientColors = if (portfolio.isPositivePerformance) {
        listOf(
            Color.Green.copy(alpha = 0.3f),
            Color.Green.copy(alpha = 0.1f)
        )
    } else {
        listOf(
            Color.Red.copy(alpha = 0.3f),
            Color.Red.copy(alpha = 0.1f)
        )
    }

    val pointerModifier = Modifier.pointerInput(portfolioHistory) {
        detectTapGestures(
            onTap = { offset ->
                val width = size.width
                val step = width / (portfolioHistory.size - 1).coerceAtLeast(1)
                val index = (offset.x / step).toInt().coerceIn(0, portfolioHistory.lastIndex)
                selectedIndex = index
                showTooltip = true
            }
        )

        detectDragGestures(
            onDragStart = { position ->
                showTooltip = true
                val width = size.width
                val step = width / (portfolioHistory.size - 1).coerceAtLeast(1)
                val index = (position.x / step).toInt().coerceIn(0, portfolioHistory.lastIndex)
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
            val step = width / (portfolioHistory.size - 1).coerceAtLeast(1)
            val xPosition = change.position.x
            val index = (xPosition / step).toInt().coerceIn(0, portfolioHistory.lastIndex)
            selectedIndex = index
        }
    }

    Canvas(
        modifier = modifier
            .then(pointerModifier)
            .padding(vertical = 8.dp)
    ) {
        if (portfolioHistory.isEmpty() || valueRange == 0.0) {
            // Draw flat line at initial balance if no data
            val y = size.height / 2
            drawLine(
                color = Color.Gray,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 2.dp.toPx()
            )
            return@Canvas
        }

        val width = size.width
        val height = size.height
        val step = width / (portfolioHistory.size - 1).coerceAtLeast(1)

        // Create path for the portfolio value line
        val linePath = androidx.compose.ui.graphics.Path().apply {
            val animatedSize = (portfolioHistory.size * animationProgress).toInt().coerceAtLeast(1)
            portfolioHistory.take(animatedSize).forEachIndexed { index, value ->
                val x = index * step
                val y = if (valueRange > 0) {
                    height - ((value - minValue) / valueRange * height).toFloat()
                } else {
                    height / 2 // Center line if no range
                }

                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }

        // Create path for gradient area (under the line)
        val gradientPath = androidx.compose.ui.graphics.Path().apply {
            val animatedSize = (portfolioHistory.size * gradientProgress).toInt().coerceAtLeast(1)

            // Start from bottom-left
            moveTo(0f, height)

            // Follow the portfolio value line
            portfolioHistory.take(animatedSize).forEachIndexed { index, value ->
                val x = index * step
                val y = if (valueRange > 0) {
                    height - ((value - minValue) / valueRange * height).toFloat()
                } else {
                    height / 2
                }
                lineTo(x, y)
            }

            // Close the path by going to bottom-right and back to start
            if (animatedSize > 0) {
                lineTo((animatedSize - 1) * step, height)
            }
            close()
        }

        // Draw gradient area under the line
        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = performanceGradientColors,
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

        // Draw portfolio value line with enhanced styling
        drawPath(
            path = linePath,
            color = performanceLineColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw glow effect for the line
        drawPath(
            path = linePath,
            color = performanceLineColor.copy(alpha = 0.3f),
            style = Stroke(
                width = 8.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw selected point with enhanced styling
        selectedIndex?.let { index ->
            if (index in portfolioHistory.indices) {
                val x = index * step
                val y = if (valueRange > 0) {
                    height - ((portfolioHistory[index] - minValue) / valueRange * height).toFloat()
                } else {
                    height / 2
                }

                // Draw outer glow
                drawCircle(
                    color = performanceLineColor.copy(alpha = 0.3f),
                    radius = 16.dp.toPx(),
                    center = Offset(x, y)
                )

                // Draw main point
                drawCircle(
                    color = performanceLineColor,
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )

                // Draw inner highlight
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )

                // Draw value tooltip
                if (showTooltip) {
                    drawContext.canvas.nativeCanvas.apply {
                        val valueText = NumberFormat.getCurrencyInstance(Locale.US)
                            .format(portfolioHistory[index])
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 32f
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            setShadowLayer(8f, 0f, 2f, android.graphics.Color.BLACK)
                        }

                        val textBounds = android.graphics.Rect()
                        textPaint.getTextBounds(valueText, 0, valueText.length, textBounds)

                        val padding = 12.dp.toPx()
                        val tooltipWidth = textBounds.width() + padding * 2
                        val tooltipHeight = textBounds.height() + padding * 2

                        val tooltipX = (x - tooltipWidth / 2).coerceIn(0f, width - tooltipWidth)
                        val tooltipY = (y - tooltipHeight - 16.dp.toPx()).coerceAtLeast(0f)

                        // Draw tooltip background
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.8f),
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipWidth, tooltipHeight),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )

                        // Draw tooltip text
                        drawText(
                            valueText,
                            tooltipX + tooltipWidth / 2,
                            tooltipY + tooltipHeight / 2 + textBounds.height() / 2,
                            textPaint
                        )
                    }
                }
            }
        }

        // Draw initial balance line for reference
        if (valueRange > 0) {
            val initialBalanceY =
                height - ((Portfolio.INITIAL_BALANCE - minValue) / valueRange * height).toFloat()
            drawLine(
                color = Color.Gray.copy(alpha = 0.5f),
                start = Offset(0f, initialBalanceY),
                end = Offset(width, initialBalanceY),
                strokeWidth = 1.dp.toPx(),
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                    floatArrayOf(
                        10f,
                        10f
                    )
                )
            )
        }
    }
}

// Generate mock portfolio history based on current portfolio state and time range
private fun generatePortfolioHistory(portfolio: Portfolio, timeRange: TimeRange): List<Double> {
    val dataPoints = when (timeRange) {
        TimeRange.ONE_DAY -> 24
        TimeRange.ONE_WEEK -> 7
        TimeRange.ONE_YEAR -> 365
        TimeRange.ALL -> 365 // Using 365 data points for 'ALL' for consistency with 1Y
    }

    val currentValue = portfolio.totalPortfolioValue
    val performancePercentage = portfolio.portfolioPerformancePercentage / 100.0

    // If no holdings, return flat line at initial balance
    if (portfolio.holdings.isEmpty()) {
        return (0 until dataPoints).map { Portfolio.INITIAL_BALANCE }
    }

    // Generate realistic-looking portfolio growth curve
    return (0 until dataPoints).map { index ->
        val progress = index.toDouble() / (dataPoints - 1)

        // Add some realistic volatility using Random.nextDouble()
        val volatility =
            kotlin.math.sin(progress * 20) * (currentValue * 0.05) * kotlin.random.Random.nextDouble(
                -1.0,
                1.0
            )

        // Linear interpolation from initial balance to current value
        val baseValue =
            Portfolio.INITIAL_BALANCE + (currentValue - Portfolio.INITIAL_BALANCE) * progress

        // Add volatility but ensure end value matches current
        if (index == dataPoints - 1) {
            currentValue // Ensure last point is exactly current value
        } else {
            (baseValue + volatility).coerceAtLeast(Portfolio.INITIAL_BALANCE * 0.5) // Don't go below 50% of initial
        }
    }
}