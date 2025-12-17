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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.School
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
import com.decagon.wallet.presentation.components.RowContainer
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.metallicBorder

/**
 * Site row for trending sites list.
 */

@Composable
fun SiteRow(
    rank: Int,
    name: String,
    category: String,
    logoUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RowContainer(onClick = onClick, modifier = modifier) {
        // LEFT: Rank + Icon + Info
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            // Rank Badge (same style as tokens)
            Box(
                modifier = Modifier.size(Dimensions.Avatar.small),
                contentAlignment = Alignment.Center
            ) {
                if (rank <= 3) {
                    val medalColor = when (rank) {
                        1 -> Color(0xFFC9B037) // Gold
                        2 -> Color(0xFFC0C0C0) // Silver
                        else -> Color(0xFFCD7F32) // Bronze
                    }
                    Icon(
                        Icons.Rounded.Stars,
                        contentDescription = null,
                        tint = medalColor,
                        modifier = Modifier
                            .size(Dimensions.IconSize.large)
                            .shadow(2.dp, CircleShape)
                    )
                    Text(
                        rank.toString(),
                        style = AppTypography.labelSmall,
                        color = Color.Black
                    )
                } else {
                    Text(
                        rank.toString(),
                        style = AppTypography.labelLarge,
                        color = AppColors.TextSecondary
                    )
                }
            }

            // Icon with rounded corners (not circle)
            var showFallback by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
                    .background(AppColors.SurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "$name Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onLoading = { showFallback = false },
                    onSuccess = { showFallback = false },
                    onError = { showFallback = true }
                )

                if (showFallback || logoUrl.isNullOrBlank()) {
                    Text(
                        name.take(1).uppercase(),
                        style = AppTypography.titleMedium,
                        color = AppColors.TextPrimary
                    )
                }
            }

            // Name + Category
            Column {
                Text(
                    name,
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    category,
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        // RIGHT: Arrow Icon
        Box(
            modifier = Modifier
                .size(Dimensions.Avatar.small)
                .clip(CircleShape)
                .background(AppColors.SurfaceHighlight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(Dimensions.IconSize.small)
            )
        }
    }
}
