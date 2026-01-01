package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.wallet.presentation.components.MetallicCard

/**
 * Swap input/output card pair.
 * NOW USES: Design system values.
 */
@Composable
fun SwapCard(
    payingToken: String,
    payingAmount: String,
    receivingToken: String,
    receivingAmount: String,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
        ) {
            // Pay Card
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "Pay",
                        style = AppTypography.labelMedium,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (payingAmount.isEmpty()) "0" else payingAmount,
                            style = AppTypography.tokenAmount,
                            color = if (payingAmount.isEmpty()) AppColors.TextTertiary else AppColors.TextPrimary
                        )
                        PriceChangeBadge(changePercent = 0.0) // Token badge
                    }
                }
            }

            // Receive Card
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        "Receive",
                        style = AppTypography.labelMedium,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (receivingAmount.isEmpty()) "0" else receivingAmount,
                            style = AppTypography.tokenAmount,
                            color = if (receivingAmount.isEmpty()) AppColors.TextTertiary else AppColors.TextPrimary
                        )
                        PriceChangeBadge(changePercent = 0.0)
                    }
                }
            }
        }

        // Floating Swap Button
        Box(
            modifier = Modifier
                .size(Dimensions.Button.iconButtonSize)
                .clip(CircleShape)
                .background(AppColors.SurfaceHighlight)
                .clickable(onClick = onFlip),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.SwapVert,
                contentDescription = "Swap",
                tint = AppColors.TextPrimary,
                modifier = Modifier.size(Dimensions.IconSize.large)
            )
        }
    }
}