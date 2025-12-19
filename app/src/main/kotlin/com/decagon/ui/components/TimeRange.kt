package com.decagon.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import java.text.NumberFormat
import java.util.Locale

enum class TimeRange(val displayName: String, val dataPoints: Int) {
    ONE_DAY("1D", 24),
    ONE_WEEK("1W", 7),
    ONE_MONTH("1M", 30),
    ONE_YEAR("1Y", 365),
    ALL("ALL", 365)
}

@Composable
private fun ChartCanvas(
    currentBalance: Double,
    timeRange: TimeRange,
    modifier: Modifier = Modifier
) {
    val historyData = remember(currentBalance, timeRange) {
        generatePortfolioHistory(currentBalance, timeRange)
    }
    
    val maxValue = remember(historyData) { historyData.maxOrNull() ?: currentBalance }
    val minValue = remember(historyData) { historyData.minOrNull() ?: currentBalance }
    val valueRange = remember(historyData) { maxValue - minValue }
    
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var showTooltip by remember { mutableStateOf(false) }
    
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
    
    val isPositive = currentBalance >= historyData.firstOrNull() ?: currentBalance
    val lineColor = if (isPositive) Color.Green else Color.Red
    val gradientColors = if (isPositive) {
        listOf(Color.Green.copy(alpha = 0.3f), Color.Green.copy(alpha = 0.1f))
    } else {
        listOf(Color.Red.copy(alpha = 0.3f), Color.Red.copy(alpha = 0.1f))
    }
    
    Canvas(
        modifier = modifier
            .pointerInput(historyData) {
                detectTapGestures { offset ->
                    val width = size.width
                    val step = width / (historyData.size - 1).coerceAtLeast(1)
                    val index = (offset.x / step).toInt().coerceIn(0, historyData.lastIndex)
                    selectedIndex = index
                    showTooltip = true
                }
            }
            .pointerInput(historyData) {
                detectDragGestures(
                    onDragStart = { position ->
                        showTooltip = true
                        val step = size.width / (historyData.size - 1).coerceAtLeast(1)
                        selectedIndex = (position.x / step).toInt().coerceIn(0, historyData.lastIndex)
                    },
                    onDragEnd = { showTooltip = false; selectedIndex = null },
                    onDragCancel = { showTooltip = false; selectedIndex = null }
                ) { change, _ ->
                    val step = size.width / (historyData.size - 1).coerceAtLeast(1)
                    selectedIndex = (change.position.x / step).toInt().coerceIn(0, historyData.lastIndex)
                }
            }
    ) {
        if (historyData.isEmpty() || valueRange == 0.0) return@Canvas
        
        val width = size.width
        val height = size.height
        val step = width / (historyData.size - 1).coerceAtLeast(1)
        
        // Line path
        val linePath = Path().apply {
            val animatedSize = (historyData.size * animationProgress).toInt().coerceAtLeast(1)
            historyData.take(animatedSize).forEachIndexed { index, value ->
                val x = index * step
                val y = height - ((value - minValue) / valueRange * height).toFloat()
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        
        // Gradient path
        val gradientPath = Path().apply {
            val animatedSize = (historyData.size * gradientProgress).toInt().coerceAtLeast(1)
            moveTo(0f, height)
            historyData.take(animatedSize).forEachIndexed { index, value ->
                val x = index * step
                val y = height - ((value - minValue) / valueRange * height).toFloat()
                lineTo(x, y)
            }
            if (animatedSize > 0) lineTo((animatedSize - 1) * step, height)
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
        
        // Draw grid
        val gridColor = Color.Gray.copy(alpha = 0.2f)
        for (i in 1..4) {
            val y = height * i / 5
            drawLine(gridColor, Offset(0f, y), Offset(width, y), 1.dp.toPx())
        }
        
        // Draw line
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        
        // Draw selected point
        selectedIndex?.let { index ->
            if (index in historyData.indices) {
                val x = index * step
                val y = height - ((historyData[index] - minValue) / valueRange * height).toFloat()
                
                drawCircle(lineColor.copy(alpha = 0.3f), 16.dp.toPx(), Offset(x, y))
                drawCircle(lineColor, 8.dp.toPx(), Offset(x, y))
                drawCircle(Color.White.copy(alpha = 0.8f), 4.dp.toPx(), Offset(x, y))
                
                if (showTooltip) {
                    drawContext.canvas.nativeCanvas.apply {
                        val valueText = NumberFormat.getCurrencyInstance(Locale.US).format(historyData[index])
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
                        
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.8f),
                            topLeft = Offset(tooltipX, tooltipY),
                            size = Size(tooltipWidth, tooltipHeight),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                        
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
    }
}

@Composable
internal fun TimeRangeSelector(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selectedTimeRange
            FilterChip(
                selected = isSelected,
                onClick = { onTimeRangeSelected(range) },
                label = { Text(range.displayName) }
            )
        }
    }
}

private fun generatePortfolioHistory(currentBalance: Double, timeRange: TimeRange): List<Double> {
    val dataPoints = timeRange.dataPoints
    val volatility = 0.05
    
    return (0 until dataPoints).map { index ->
        val progress = index.toDouble() / (dataPoints - 1)
        val baseValue = currentBalance * (0.8 + progress * 0.2)
        val noise = kotlin.math.sin(progress * 20) * (currentBalance * volatility) * 
                    kotlin.random.Random.nextDouble(-1.0, 1.0)
        
        if (index == dataPoints - 1) currentBalance
        else (baseValue + noise).coerceAtLeast(currentBalance * 0.5)
    }
}