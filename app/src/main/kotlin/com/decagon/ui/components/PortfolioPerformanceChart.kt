package com.decagon.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.PortfolioHistoryPoint
import kotlin.math.roundToInt

@Composable
fun PortfolioPerformanceChart(
    historyPoints: List<PortfolioHistoryPoint>,
    selectedTimeRange: TimeRange,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val animationProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "chart_animation"
    )

    if (historyPoints.isEmpty()) {
        EmptyChartPlaceholder(modifier)
        return
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .pointerInput(historyPoints) {
                detectTapGestures { offset ->
                    val step = size.width / (historyPoints.size - 1).coerceAtLeast(1)
                    val index = (offset.x / step).roundToInt()
                        .coerceIn(0, historyPoints.lastIndex)
                    selectedIndex = index
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val step = width / (historyPoints.size - 1).coerceAtLeast(1)

        val maxValue = historyPoints.maxOf { it.totalValueUsd }
        val minValue = historyPoints.minOf { it.totalValueUsd }
        val valueRange = (maxValue - minValue).coerceAtLeast(0.01)

        // Create path for line and gradient
        val linePath = Path()
        val gradientPath = Path()

        val animatedPoints = historyPoints.take(
            (historyPoints.size * animationProgress).toInt().coerceAtLeast(1)
        )

        animatedPoints.forEachIndexed { index, point ->
            val x = index * step
            val y = height - ((point.totalValueUsd - minValue) / valueRange * height * 0.85f).toFloat()

            if (index == 0) {
                linePath.moveTo(x, y)
                gradientPath.moveTo(x, height)
                gradientPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                gradientPath.lineTo(x, y)
            }
        }

        // Close gradient path
        gradientPath.lineTo(animatedPoints.lastIndex * step, height)
        gradientPath.close()

        // Draw gradient fill
        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF9945FF).copy(alpha = 0.35f),
                    Color(0xFF9945FF).copy(alpha = 0.05f)
                ),
                startY = 0f,
                endY = height
            )
        )

        // Draw line with glow
        drawPath(
            path = linePath,
            color = Color(0xFF9945FF).copy(alpha = 0.3f),
            style = Stroke(
                width = 6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        drawPath(
            path = linePath,
            color = Color(0xFF9945FF),
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // Draw selected point indicator
        selectedIndex?.let { index ->
            if (index in historyPoints.indices) {
                val x = index * step
                val y = height - ((historyPoints[index].totalValueUsd - minValue) / valueRange * height * 0.85f).toFloat()

                // Outer glow
                drawCircle(
                    color = Color(0xFF9945FF).copy(alpha = 0.2f),
                    radius = 18.dp.toPx(),
                    center = Offset(x, y)
                )

                // Inner dot
                drawCircle(
                    color = Color(0xFF9945FF),
                    radius = 8.dp.toPx(),
                    center = Offset(x, y)
                )

                // White center
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            "Loading chart data...",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF7E7E8F)
        )
    }
}

//enum class TimeRange(val displayName: String) {
//    ONE_DAY("1D"),
//    ONE_WEEK("1W"),
//    ONE_MONTH("1M"),
//    ONE_YEAR("1Y"),
//    ALL("ALL")
//}

@Composable
fun TimeRangeSelector(
    selected: TimeRange,
    onSelect: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TimeRange.values().forEach { range ->
            TimeRangeChip(
                label = range.displayName,
                isSelected = range == selected,
                onClick = { onSelect(range) }
            )
        }
    }
}

@Composable
private fun TimeRangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9945FF).copy(alpha = 0.3f),
                            Color(0xFF9945FF).copy(alpha = 0.15f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A34).copy(alpha = 0.4f),
                            Color(0xFF1A1A24).copy(alpha = 0.4f)
                        )
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isSelected) Color(0xFF9945FF) else Color(0xFFB4B4C6),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        )
    }
}