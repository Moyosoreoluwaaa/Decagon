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

    // Resolve colors in the Composable context
    val primaryColor = MaterialTheme.colorScheme.primary
    val dotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)

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
            .padding(vertical = 16.dp)
            .pointerInput(historyPoints) {
                detectTapGestures { offset ->
                    val step = size.width / (historyPoints.size - 1).coerceAtLeast(1)
                    selectedIndex = (offset.x / step).roundToInt()
                        .coerceIn(0, historyPoints.lastIndex)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val step = width / (historyPoints.size - 1).coerceAtLeast(1)

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

        val maxValue = historyPoints.maxOf { it.totalValueUsd }
        val minValue = historyPoints.minOf { it.totalValueUsd }
        val valueRange = (maxValue - minValue).coerceAtLeast(0.01)

        val linePath = Path()
        val gradientPath = Path()
        val animatedPoints = historyPoints.take(
            (historyPoints.size * animationProgress).toInt().coerceAtLeast(1)
        )

        animatedPoints.forEachIndexed { index, point ->
            val x = index * step
            val y =
                height - ((point.totalValueUsd - minValue) / valueRange * height * 0.85f).toFloat()

            if (index == 0) {
                linePath.moveTo(x, y)
                gradientPath.moveTo(x, height)
                gradientPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                gradientPath.lineTo(x, y)
            }
        }

        gradientPath.lineTo(animatedPoints.lastIndex * step, height)
        gradientPath.close()

        drawPath(
            path = gradientPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.2f), Color.Transparent),
                startY = 0f, endY = height
            )
        )

        drawPath(
            path = linePath,
            color = primaryColor,
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        selectedIndex?.let { index ->
            if (index in historyPoints.indices) {
                val x = index * step
                val y =
                    height - ((historyPoints[index].totalValueUsd - minValue) / valueRange * height * 0.85f).toFloat()
                drawCircle(primaryColor.copy(alpha = 0.2f), 16.dp.toPx(), Offset(x, y))
                drawCircle(primaryColor, 6.dp.toPx(), Offset(x, y))
                drawCircle(Color.White, 3.dp.toPx(), Offset(x, y))
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