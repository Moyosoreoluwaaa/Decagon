package com.decagon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

@Composable
fun AssetDetailHeader(
    name: String,
    symbol: String,
    logoUrl: String?,
    currentPrice: String,
    priceChangePercent: Double,
    scrollProgress: Float,
    modifier: Modifier = Modifier
) {
    val headerAlpha = 1f - scrollProgress
    val headerScale = 1f - (scrollProgress * 0.05f) // Reduced scale effect for stability

    Row(
        modifier = modifier
            .fillMaxWidth() // Ensures it takes full width
            .alpha(headerAlpha)
            .scale(headerScale)
            .padding(vertical = Dimensions.Padding.standard),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Pushes content to edges
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            AsyncImage(
                model = logoUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(Dimensions.Spacing.standard))
            Column {
                Text(
                    text = symbol.uppercase(),
                    style = AppTypography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = name,
                    style = AppTypography.bodyMedium,
                    color = AppColors.TextSecondary
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = currentPrice,
                style = AppTypography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${if (priceChangePercent >= 0) "+" else ""}${String.format("%.2f", priceChangePercent)}%",
                color = if (priceChangePercent >= 0) AppColors.Success else AppColors.Error,
                style = AppTypography.titleMedium
            )
        }
    }
}