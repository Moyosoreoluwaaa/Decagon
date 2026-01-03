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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.decagon.ui.theme.AppColors
import com.decagon.ui.theme.AppTypography
import com.decagon.ui.theme.Dimensions

/**
 * Site row for trending sites list.
 */
@Composable
fun SiteRow(
    name: String,
    category: String,
    logoUrl: String?,
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
            )
            Text(
                category,
                style = AppTypography.bodySmall,
            )
        }

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
