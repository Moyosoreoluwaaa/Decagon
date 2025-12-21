// presentation/components/DetailUtilCard.kt

package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Utility card for token detail stats.
 */
@Composable
fun DetailUtilCard(
    header: String,
    subHeader: String,
    modifier: Modifier = Modifier
) {
    UtilMetallicCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall)
        ) {
            Text(
                text = if (header.isEmpty()) "0" else header,
                style = AppTypography.titleMedium,
                color = if (header.isEmpty()) AppColors.TextTertiary else AppColors.TextPrimary
            )
            Text(
                text = if (subHeader.isEmpty()) "0" else subHeader,
                style = AppTypography.bodyMedium,
                color = if (subHeader.isEmpty()) AppColors.TextTertiary else AppColors.TextSecondary
            )
        }
    }
}
