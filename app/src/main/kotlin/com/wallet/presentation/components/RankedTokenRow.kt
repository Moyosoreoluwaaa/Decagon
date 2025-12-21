package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
 * Ranked token row for trending tokens list.
 * Shows rank badge (medal for top 3), market cap, price, and change.
 */
@Composable
fun RankedTokenRow(
    rank: Int,
    symbol: String,
    name: String,
    marketCap: String,
    price: String,
    changePercent: Double,
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LEFT: Rank Badge
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (rank <= 3) {
                // Medal for top 3
                val medalColor = when (rank) {
                    1 -> Color(0xFFFFD700) // Gold
                    2 -> Color(0xFFC0C0C0) // Silver
                    else -> Color(0xFFCD7F32) // Bronze
                }

                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Stars,
                        contentDescription = null,
                        tint = medalColor,
                        modifier = Modifier
                            .size(32.dp)
                            .shadow(2.dp, CircleShape)
                    )
                    Text(
                        rank.toString(),
                        style = AppTypography.labelSmall,
                        color = Color.Black
                    )
                }
            } else {
                // Plain number for others
                Text(
                    rank.toString(),
                    style = AppTypography.labelLarge,
                    color = AppColors.TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.width(Dimensions.Spacing.small))

        // MIDDLE: Token Icon (AsyncImage) + Name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            modifier = Modifier.weight(1f)
        ) {
            // ⭐ ASYNC IMAGE INTEGRATION WITH FALLBACK CORRECTION ⭐
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
                        .data(logoUrl) // Use the logo URL
                        .crossfade(true)
                        .build(),
                    contentDescription = "$symbol Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    // Set fallback state on error or if data is null/empty
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
            // ⭐ END ASYNC IMAGE INTEGRATION ⭐

            Column {
                Text(
                    symbol.uppercase(),
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    marketCap,
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1
                )
            }
        }


        Spacer(modifier = Modifier.width(Dimensions.Spacing.large))

        // FAR RIGHT: Price + Change
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(min = 80.dp)
        ) {
            Text(
                price,
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            PriceChangeBadge(changePercent = changePercent)
        }
    }
}