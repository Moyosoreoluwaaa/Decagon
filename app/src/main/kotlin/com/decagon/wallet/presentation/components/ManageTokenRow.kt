package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Token row with visibility toggle.
 * Used in Manage Tokens screen.
 */
@Composable
fun ManageTokenRow(
    symbol: String,
    name: String,
    balance: String,
    isEnabled: Boolean,
    iconColor: Color,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
            .background(AppColors.Background)
            .padding(Dimensions.Padding.tiny),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Dimensions.Avatar.medium)
                    .clip(CircleShape)
                    .background(iconColor)
            ) {
                Text(
                    symbol.take(1),
                    style = AppTypography.labelLarge,
                    color = Color.White
                )
            }
            
            Column {
                Text(
                    name,
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Text(
                    balance,
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }
        
        IndustrialSwitch(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}