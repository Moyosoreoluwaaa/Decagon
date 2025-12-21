package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters
import com.octane.wallet.presentation.utils.metallicBorder

/**
 * Token row component for asset lists.
 * NOW USES: Design system values & formatting utilities.
 */
@Composable
fun TokenRow(
    symbol: String,
    name: String,
    balance: String,
    valueUsd: Double,
    changePercent: Double,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
            .background(AppColors.Background)
            .metallicBorder(
                Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.standard),
                angleDeg = 90f
            )
            .clickable(onClick = onClick)
            .padding(Dimensions.Padding.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: Icon + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            // Token Icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Dimensions.Avatar.medium)
                    .clip(CircleShape)
                    .background(iconColor)
            ) {
                Text(
                    symbol.take(1),
                    style = AppTypography.labelLarge,
                    color = Color.White
                )
            }

            Column {
                Text(
                    symbol,
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    balance,
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        // RIGHT: Value + Change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                UiFormatters.formatUsd(valueUsd),
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            PriceChangeBadge(changePercent = changePercent)
        }
    }
}