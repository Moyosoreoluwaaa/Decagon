package com.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.metallicBorder

/**
 * Base metallic card component.
 * Standard card for main content sections.
 */
@Composable
fun MetallicCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = Dimensions.CornerRadius.extraLarge,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(AppColors.Surface)
            .metallicBorder(
                width = Dimensions.Border.standard,
                shape = RoundedCornerShape(cornerRadius),
                angleDeg = 170f // Card angle
            )
            .padding(Dimensions.Padding.standard),
        content = content
    )
}

/**
 * Utility card (smaller, for stats/metrics).
 */
@Composable
fun UtilMetallicCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.CornerRadius.small))
            .background(AppColors.Surface)
            .metallicBorder(
                width = Dimensions.Border.thin,
                shape = RoundedCornerShape(Dimensions.CornerRadius.standard),
                angleDeg = 170f
            )
            .padding(Dimensions.Padding.medium),
        content = content
    )
}