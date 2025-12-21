package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.metallicBorder

/**
 * Mode selector tabs (Tokens, Perps, Lists).
 */
@Composable
fun ModeSelectorTabs(
    modes: List<String>,
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.Padding.standard),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        modes.forEach { mode ->
            val isSelected = mode == selectedMode
            val cornerRadius = Dimensions.CornerRadius.standard
            Box(
                modifier = Modifier
                    .metallicBorder(
                        width = Dimensions.Border.standard,
                        shape = RoundedCornerShape(cornerRadius),
                        angleDeg = 170f // Card angle
                    )
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.pill))
                    .background(if (isSelected) AppColors.TextPrimary else AppColors.Surface)
                    .clickable { onModeSelected(mode) }
                    .padding(
                        horizontal = Dimensions.Padding.large,
                        vertical = Dimensions.Padding.medium
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall)
                ) {
                    if (mode == "Perps") {
                        Icon(
                            Icons.Rounded.AllInclusive,
                            contentDescription = null,
                            tint = if (isSelected) Color.Black else AppColors.TextPrimary,
                            modifier = Modifier.size(Dimensions.IconSize.small)
                        )
                    }

                    Text(
                        text = mode,
                        style = AppTypography.labelLarge,
                        color = if (isSelected) Color.Black else AppColors.TextPrimary
                    )
                }
            }
        }
    }
}