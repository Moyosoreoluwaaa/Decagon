package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Bottom sheet shown when user clicks quick actions without wallet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletActionBottomSheet(
    onDismiss: () -> Unit,
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Background,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Padding.extraLarge)
                .padding(bottom = Dimensions.Padding.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(AppColors.SurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.AccountBalanceWallet,
                    contentDescription = null,
                    tint = AppColors.TextPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
            
            // Text
            Text(
                "Wallet Required",
                style = AppTypography.headlineMedium,
                color = AppColors.TextPrimary
            )
            Text(
                "Create or import a wallet to use this feature",
                style = AppTypography.bodyMedium,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
            
            // Buttons
            Button(
                onClick = {
                    onDismiss()
                    onCreateWallet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.Button.heightLarge),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Success
                )
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
                onClick = {
                    onDismiss()
                    onImportWallet()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.Button.heightLarge)
            ) {
                Icon(
                    Icons.Rounded.Download,
                    contentDescription = null,
                    modifier = Modifier.size(Dimensions.IconSize.medium)
                )
                Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                Text("Import Wallet", style = AppTypography.labelLarge)
            }
        }
    }
}