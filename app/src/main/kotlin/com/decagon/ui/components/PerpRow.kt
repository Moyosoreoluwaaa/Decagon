package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.octane.wallet.presentation.components.PriceChangeBadge
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

@Composable
fun PerpRow(
    symbol: String,
    name: String,
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
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(Dimensions.Padding.standard),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            modifier = Modifier.weight(1f)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                var showFallback by remember { mutableStateOf(false) }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(Dimensions.Avatar.large)
                        .clip(CircleShape)
                        .background(fallbackIconColor)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(logoUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "$symbol Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        onLoading = { showFallback = false },
                        onSuccess = { showFallback = false },
                        onError = { showFallback = true }
                    )

                    if (showFallback || logoUrl.isNullOrBlank()) {
                        Text(
                            symbol.take(1),
                            style = AppTypography.labelLarge,
                        )
                    }
                }

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

            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    symbol.uppercase(),
                    style = AppTypography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        volume24h,
                        style = AppTypography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "•",
                        style = AppTypography.bodySmall,
                    )
                    Text(
                        "${leverageMax}x",
                        style = AppTypography.bodySmall,
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.widthIn(max = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                price,
                style = AppTypography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            PriceChangeBadge(changePercent = changePercent)
        }
    }
}