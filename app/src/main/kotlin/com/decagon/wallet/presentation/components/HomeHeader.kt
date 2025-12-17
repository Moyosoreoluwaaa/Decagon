package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.metallicBorder

/**
 * ✅ Updated Home header with Settings icon and proper wallet icon display.
 */
@Composable
fun HomeHeader(
    totalBalance: String,
    changeAmount: String,
    changePercent: Double,
    walletName: String?,
    walletIcon: String?, // ✅ NEW: Wallet emoji/icon
    onWalletClick: () -> Unit = {},
    onActivityClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}, // ✅ NEW: Settings click
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Spacing.medium)
    ) {
        // Top Bar: Wallet Selector + Action Icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Wallet selector - only show if wallet exists
            if (walletName != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
                        .background(AppColors.Surface)
                        .metallicBorder(
                            Dimensions.Border.standard,
                            RoundedCornerShape(Dimensions.CornerRadius.medium),
                            90f
                        )
                        .clickable(onClick = onWalletClick)
                        .padding(
                            horizontal = Dimensions.Padding.medium,
                            vertical = Dimensions.Padding.small
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                ) {
                    // ✅ Wallet icon/emoji
                    Box(
                        modifier = Modifier
                            .size(Dimensions.Avatar.small)
                            .clip(CircleShape)
                            .background(AppColors.SurfaceHighlight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            walletIcon ?: walletName.take(1).uppercase(),
                            style = AppTypography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        walletName,
                        style = AppTypography.labelLarge,
                        color = AppColors.TextPrimary,
                        fontWeight = FontWeight.Medium
                    )

                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Switch wallet",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(Dimensions.IconSize.small)
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            // ✅ Action Icons: Activity + Settings (replaced Search)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)) {
                Icon(
                    Icons.Rounded.History,
                    contentDescription = "Activity",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier
                        .size(Dimensions.IconSize.standard)
                        .clickable(onClick = onActivityClick)
                )
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    tint = AppColors.TextPrimary,
                    modifier = Modifier
                        .size(Dimensions.IconSize.standard)
                        .clickable(onClick = onSettingsClick)
                )
            }
        }

        Spacer(modifier = Modifier.height(Dimensions.Spacing.extraLarge))

        // Balance Display
        Text(
            text = totalBalance,
            style = AppTypography.balanceDisplay,
            color = AppColors.TextPrimary
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
        ) {
            Text(
                changeAmount,
                style = AppTypography.bodyMedium,
                color = AppColors.TextSecondary
            )
            PriceChangeBadge(changePercent = changePercent)
        }
    }
}