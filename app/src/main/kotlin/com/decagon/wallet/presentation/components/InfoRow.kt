package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Information row with label and value.
 * Used in detail screens for stats display.
 */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.Padding.small),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = AppTypography.bodyMedium,
                color = AppColors.TextSecondary
            )
            Text(
                text = value,
                style = AppTypography.bodyMedium,
                color = AppColors.TextPrimary
            )
        }
        
        if (showDivider) {
            // Metallic divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.Border.thin)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black,
                                AppColors.BorderDefault,
                                Color.Black
                            )
                        )
                    )
            )
        }
    }
}