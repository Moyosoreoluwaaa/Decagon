package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.metallicBorder

/**
 * Empty wallet state hero section.
 */
@Composable
fun EmptyWalletHero(
    onBuyCash: () -> Unit,
    onDepositCrypto: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Spacing.extraLarge)
    ) {
        // Illustration
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(AppColors.SurfaceHighlight)
                .metallicBorder(
                    Dimensions.Border.standard,
                    CircleShape,
                    135f
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = AppColors.TextSecondary,
                modifier = Modifier.size(Dimensions.IconSize.huge)
            )
        }
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
        
        // Text
        Text(
            "Your wallet is ready",
            style = AppTypography.headlineSmall,
            color = AppColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
        Text(
            "Fund your wallet with cash or crypto and\nyou'll be set to start trading!",
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = AppTypography.bodyMedium.lineHeight
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.extraLarge))
        
        // Buttons
        WideActionButton(
            text = "Buy SOL with Cash",
            isPrimary = true,
            onClick = onBuyCash
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        WideActionButton(
            text = "Deposit Crypto",
            isPrimary = false,
            onClick = onDepositCrypto
        )
    }
}