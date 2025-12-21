package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

@Composable
internal fun EmptyWalletsState(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimensions.Padding.extraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Rounded.AccountBalanceWallet,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = AppColors.TextTertiary
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.large))

        Text(
            "No Wallets Yet",
            style = AppTypography.headlineMedium,
            color = AppColors.TextPrimary
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

        Text(
            "Create your first wallet or import an existing one",
            style = AppTypography.bodyLarge,
            color = AppColors.TextSecondary
        )

        Spacer(modifier = Modifier.height(Dimensions.Spacing.extraLarge))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
        ) {
            Button(
                onClick = onCreateWallet,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Success
                ),
                modifier = Modifier.height(Dimensions.Button.heightLarge)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSize.medium)
                )
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                Text("Create Wallet", style = AppTypography.labelLarge)
            }

            OutlinedButton(
                onClick = onImportWallet,
                modifier = Modifier.height(Dimensions.Button.heightLarge)
            ) {
                Icon(
                    Icons.Rounded.Download,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSize.medium)
                )
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                Text("Import", style = AppTypography.labelLarge)
            }
        }
    }
}


