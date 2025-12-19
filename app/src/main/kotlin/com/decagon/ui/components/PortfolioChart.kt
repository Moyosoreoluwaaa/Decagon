package com.decagon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp


@Composable
private fun PortfolioChart(
    portfolioHistory: List<Double>,
    selectedTimeRange: TimeRange,
    onTimeRangeChange: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Chart canvas
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 24.dp)
        ) {
            val width = size.width
            val height = size.height
            val step = width / (portfolioHistory.size - 1).coerceAtLeast(1)

            val maxValue = portfolioHistory.maxOrNull() ?: 0.0
            val minValue = portfolioHistory.minOrNull() ?: 0.0
            val valueRange = (maxValue - minValue).coerceAtLeast(0.01)

            // Create gradient path
            val path = Path().apply {
                moveTo(0f, height)

                portfolioHistory.forEachIndexed { index, value ->
                    val x = index * step
                    val y = height - ((value - minValue) / valueRange * height * 0.8f).toFloat()

                    if (index == 0) {
                        lineTo(x, y)
                    } else {
                        lineTo(x, y)
                    }
                }

                lineTo(width, height)
                close()
            }

            // Draw gradient fill
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF9945FF).copy(alpha = 0.4f),
                        Color(0xFF9945FF).copy(alpha = 0.05f)
                    )
                )
            )

            // Draw line stroke
            drawPath(
                path = path,
                color = Color(0xFF9945FF),
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time range selector
        TimeRangeSelector(
            selectedTimeRange = selectedTimeRange,
            onTimeRangeSelected = onTimeRangeChange
        )
    }
}