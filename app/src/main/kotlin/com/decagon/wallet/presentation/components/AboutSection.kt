package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * About section for token details.
 */
@Composable
fun AboutSection(
    description: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
    ) {
        Text(
            "About",
            style = AppTypography.titleSmall,
            color = AppColors.TextSecondary
        )
        Text(
            text = description,
            style = AppTypography.bodyMedium,
            color = AppColors.TextPrimary,
            lineHeight = AppTypography.bodyMedium.lineHeight,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis
        )
    }
}