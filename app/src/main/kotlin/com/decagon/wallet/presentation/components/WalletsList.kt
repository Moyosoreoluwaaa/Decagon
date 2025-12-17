package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.octane.wallet.domain.models.Wallet
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

@Composable
internal fun WalletsList(
    wallets: List<Wallet>,
    activeWallet: Wallet?,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    onWalletClick: (Wallet) -> Unit,
    onEditClick: (Wallet) -> Unit,
    onDeleteClick: (Wallet) -> Unit,
    onCreateClick: () -> Unit,
    onImportClick: () -> Unit,
    getWalletColor: (Wallet) -> Color,
    getWalletDisplayName: (Wallet) -> String
) {
    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimensions.Padding.standard),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        items(wallets) { wallet ->
            WalletCard(
                wallet = wallet,
                isActive = wallet.id == activeWallet?.id,
                walletColor = getWalletColor(wallet),
                displayName = getWalletDisplayName(wallet),
                onClick = { onWalletClick(wallet) },
                onEditClick = { onEditClick(wallet) },
                onDeleteClick = { onDeleteClick(wallet) }
            )
        }

        // ✅ Action buttons at bottom of list
        item {
            Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
            ) {
                OutlinedButton(
                    onClick = onImportClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.Button.heightLarge)
                ) {
                    Icon(
                        Icons.Rounded.Download,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.IconSize.medium)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                    Text("Import", style = AppTypography.labelLarge)
                }

                Button(
                    onClick = onCreateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Success
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(Dimensions.Button.heightLarge)
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = null,
                        modifier = Modifier.size(Dimensions.IconSize.medium)
                    )
                    Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                    Text("Create", style = AppTypography.labelLarge)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
        }
    }
}
