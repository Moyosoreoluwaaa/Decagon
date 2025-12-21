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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .metallicBorder(
                Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.standard),
                angleDeg = 90f
            )
            .padding(Dimensions.Padding.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank Badge (Medal Style)
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

        Spacer(modifier = Modifier.width(Dimensions.Spacing.medium))

        // ⭐ ASYNC IMAGE INTEGRATION WITH FALLBACK CORRECTION ⭐
        var showFallback by remember { mutableStateOf(false) }

        // Site Icon
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
                .background(AppColors.SurfaceHighlight), // Fallback background color
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(logoUrl) // Use the logo URL
                    .crossfade(true)
                    .build(),
                contentDescription = "$name Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.medium)),
                onLoading = { showFallback = false },
                onSuccess = { showFallback = false },
                onError = { showFallback = true }
            )

            // Conditional Fallback Content
            if (showFallback || logoUrl.isNullOrBlank()) {
                Text(
                    name.take(1),
                    style = AppTypography.titleMedium,
                    color = AppColors.TextPrimary
                )
            }
        }
        // ⭐ END ASYNC IMAGE INTEGRATION ⭐

        Spacer(modifier = Modifier.width(Dimensions.Spacing.standard))

        // Name & Category
        Column(modifier = Modifier.weight(1f)) {
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

        // Arrow
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

/**
 * Learn card for educational content. (Unchanged, as it's not a dynamic list item)
 */
@Composable
fun LearnCard(
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(AppColors.Surface)
            .metallicBorder(
                Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.large),
                angleDeg = 170f
            )
            .clickable(onClick = onClick)
            .padding(Dimensions.Padding.standard),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
    ) {
        // Large Icon
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.School,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(Dimensions.IconSize.extraLarge)
            )
        }

        Column {
            Text(
                title,
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            Text(
                subtitle,
                style = AppTypography.bodySmall,
                color = AppColors.TextSecondary
            )
        }
    }
}