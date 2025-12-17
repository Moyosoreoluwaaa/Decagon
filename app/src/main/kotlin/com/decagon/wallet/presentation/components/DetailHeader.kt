// presentation/components/DetailHeader.kt

package com.octane.wallet.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.presentation.theme.*

@Composable
fun DetailHeader(
    price: String,
    changeAmount: String,
    changePercent: Double,
    isPositive: Boolean,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    chartState: LoadingState<List<Double>>, // ✅ Chart data state
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Price Display
        Text(
            text = price,
            style = AppTypography.priceDisplay,
            color = AppColors.TextPrimary
        )

        // Change
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
        ) {
            Text(
                changeAmount,
                style = AppTypography.bodyMedium,
                color = AppColors.TextSecondary
            )
            PriceChangeBadge(changePercent = changePercent)
        }

        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

        // ✅ Chart with Loading State
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(vertical = Dimensions.Spacing.standard)
        ) {
            when (chartState) {
                is LoadingState.Loading -> {
                    // Shimmer effect
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
                            .shimmerEffect()
                    )
                }

                is LoadingState.Success -> {
                    val data = chartState.data
                    if (data.isNotEmpty()) {
                        ChartCanvas(
                            data = data,
                            lineColor = if (isPositive) AppColors.Success else AppColors.Error
                        )
                    }
                }

                is LoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Chart unavailable",
                            style = AppTypography.bodySmall,
                            color = AppColors.TextTertiary
                        )
                    }
                }

                else -> {}
            }
        }

        // Timeframe Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("1H", "1D", "1W", "1M", "YTD", "ALL").forEach { timeframe ->
                TimeframeChip(
                    label = timeframe,
                    isSelected = timeframe == selectedTimeframe,
                    onClick = { onTimeframeSelected(timeframe) }
                )
            }
        }
    }
}

@Composable
private fun ChartCanvas(
    data: List<Double>,
    lineColor: androidx.compose.ui.graphics.Color
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val minValue = data.minOrNull() ?: 0.0
        val maxValue = data.maxOrNull() ?: 1.0
        val range = maxValue - minValue

        if (range == 0.0) return@Canvas

        val path = Path().apply {
            data.forEachIndexed { index, value ->
                val x = (index.toFloat() / (data.size - 1)) * width
                val y = height - (((value - minValue) / range).toFloat() * height)

                if (index == 0) {
                    moveTo(x, y)
                } else {
                    lineTo(x, y)
                }
            }
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

@Composable
private fun TimeframeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.CornerRadius.small))
            .background(if (isSelected) AppColors.SurfaceHighlight else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimensions.Padding.medium,
                vertical = Dimensions.Padding.small
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = AppTypography.labelMedium,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary
        )
    }
}