package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.metallicBorder

/**
 * Perpetual futures row component.
 * Shows leverage badge and volume data.
 */
@Composable
fun PerpRow(
    symbol: String,
    name: String, // Kept for data, but no longer used for main title
    price: String,
    changePercent: Double,
    volume24h: String,
    leverageMax: Int,
    logoUrl: String?,
    fallbackIconColor: Color,
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
        // LEFT: Badged Icon + Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            // Icon with Infinity Badge
            Box(contentAlignment = Alignment.BottomEnd) {
                // Async Image Integration
                var showFallback by remember { mutableStateOf(false) }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(Dimensions.Avatar.large)
                        .clip(CircleShape)
                        .background(fallbackIconColor) // Fallback background color
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "$symbol Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        onLoading = { showFallback = false },
                        onSuccess = { showFallback = false },
                        onError = { showFallback = true }
                    )

                    // Conditional Fallback Content
                    if (showFallback || logoUrl.isNullOrBlank()) {
                        Text(
                            symbol.take(1),
                            style = AppTypography.labelLarge,
                            color = Color.White
                        )
                    }
                }
                // End Async Image Integration

                // Perp Badge
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.AllInclusive,
                        contentDescription = "Perp",
                        tint = Color.Black,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column {
                Text(
                    // ✅ FIXED: Use the symbol (e.g., WIP-USDC) as the main title for consistency
                    symbol.uppercase(),
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        volume24h,
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        "•",
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        "${leverageMax}x",
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }

        // RIGHT: Price + Change
        Column(horizontalAlignment = Alignment.End) {
            Text(
                price,
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            PriceChangeBadge(changePercent = changePercent)
        }
    }
}