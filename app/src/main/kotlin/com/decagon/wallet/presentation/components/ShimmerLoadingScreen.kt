package com.octane.wallet.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Full-screen shimmer loading effect for Discover screen.
 * Uses consistent card sizes: 1 SwapCard + 5 SiteCards
 */
@Composable
fun ShimmerLoadingScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .padding(horizontal = Dimensions.Padding.standard)
            .padding(top = Dimensions.Spacing.large),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
    ) {
        // Search bar shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

        // Mode tabs shimmer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
                        .shimmerEffect()
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.Spacing.standard))

        // SwapCard-sized shimmer at top
        SwapCardShimmer()

        // 5 SiteCard-sized shimmers below
        repeat(5) {
            SiteCardShimmer()
        }
    }
}

/**
 * SwapCard-sized shimmer (larger, for featured content).
 */
@Composable
private fun SwapCardShimmer(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(AppColors.Surface)
            .padding(Dimensions.Padding.standard),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        // Title shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )

        // Large amount shimmer
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

        // Secondary content
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }
    }
}

/**
 * SiteCard-sized shimmer (compact row format).
 */
@Composable
private fun SiteCardShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
            .background(AppColors.Surface)
            .padding(Dimensions.Padding.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
    ) {
        // Rank circle
        Box(
            modifier = Modifier
                .size(Dimensions.Avatar.small)
                .clip(CircleShape)
                .shimmerEffect()
        )

        // Logo circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
                .shimmerEffect()
        )

        // Text content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall)
        ) {
            // Name
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            // Category
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }

        // Arrow circle
        Box(
            modifier = Modifier
                .size(Dimensions.Avatar.small)
                .clip(CircleShape)
                .shimmerEffect()
        )
    }
}

/**
 * Shimmer effect modifier with smooth animation.
 */
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val shimmerColors = listOf(
        AppColors.SurfaceHighlight.copy(alpha = 0.3f),
        AppColors.SurfaceHighlight.copy(alpha = 0.5f),
        AppColors.SurfaceHighlight.copy(alpha = 0.3f)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnimation, 0f),
        end = Offset(translateAnimation + 200f, 0f)
    )

    return this.background(brush)
}