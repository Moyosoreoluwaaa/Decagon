package com.decagon.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.octane.wallet.presentation.components.PriceChangeBadge
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

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
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(Dimensions.Padding.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    symbol.uppercase(),
                    style = AppTypography.titleSmall,
                )
                Text(
                    marketCap,
                    style = AppTypography.bodySmall,
                    maxLines = 1
                )
            }
        }


        Spacer(modifier = Modifier.width(Dimensions.Spacing.large))

        // FAR RIGHT: Price + Change
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.widthIn(min = 80.dp)
        ) {
            Text(
                price,
                style = AppTypography.titleSmall,
            )
            PriceChangeBadge(changePercent = changePercent)
        }
    }
}