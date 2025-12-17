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
 * Shown when no wallet exists yet.
 */
@Composable
fun NoWalletHero(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Spacing.small)
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
        
        Text(
            "No wallet yet",
            style = AppTypography.headlineSmall,
            color = AppColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
        Text(
            "Create or import a wallet to get started",
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(Dimensions.Spacing.extraLarge))
        
        // Buttons
        WideActionButton(
            text = "Create Wallet",
            isPrimary = true,
            onClick = onCreateWallet
        )
        Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        WideActionButton(
            text = "Import Wallet",
            isPrimary = false,
            onClick = onImportWallet
        )
    }
}