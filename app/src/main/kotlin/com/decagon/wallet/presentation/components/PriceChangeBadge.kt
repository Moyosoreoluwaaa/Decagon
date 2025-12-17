package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters

/**
 * Badge showing price change percentage with color coding.
 * Replaces hardcoded StatusChip.
 */
@Composable
fun PriceChangeBadge(
    changePercent: Double,
    modifier: Modifier = Modifier
) {
    val (color, formatted) = UiFormatters.formatPercentageChange(changePercent)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(color.copy(alpha = 0.15f))
            .padding(
                horizontal = Dimensions.Padding.small,
                vertical = Dimensions.Padding.tiny
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = formatted,
            style = AppTypography.labelSmall,
            color = color
        )
    }
}