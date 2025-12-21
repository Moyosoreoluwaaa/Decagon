package com.octane.wallet.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Wallet
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.utils.UiFormatters
import com.octane.wallet.presentation.viewmodel.DAppBrowserViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSwitcherSheet(
    viewModel: DAppBrowserViewModel = koinViewModel(),
    onDismiss: () -> Unit,
    onWalletSelected: (Wallet) -> Unit // Callback to inform the screen/DApp
) {
    val allWalletsState by viewModel.allWallets.collectAsState()
    val activeWallet by viewModel.activeWallet.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.padding(bottom = 32.dp)
        ) {
            Text(
                text = "Switch Wallet",
                style = AppTypography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )

            when (val state = allWalletsState) {
                is LoadingState.Loading -> {
                    // Placeholder for a proper loading indicator
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(vertical = 32.dp)
                    )
                }
                is LoadingState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items (state.data) { wallet ->
                            WalletItem(
                                wallet = wallet,
                                isActive = wallet.id == activeWallet?.id,
                                onSelect = {
                                    // FIX: Removed the redundant/broken call to onWalletSelectedForSwitch
                                    // The screen (DAppBrowserScreen) will handle calling the VM function
                                    // along with the required currentUrl via the onWalletSelected callback.
                                    onWalletSelected(wallet)
                                }
                            )
                        }
                    }
                }
                is LoadingState.Error -> {
                    Text(
                        text = "Error loading wallets: ${state.message}",
                        color = AppColors.Error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                else -> {}
            }
        }
    }
}

@Composable
private fun WalletItem(
    wallet: Wallet,
    isActive: Boolean,
    onSelect: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        leadingContent = {
            // Icon Placeholder using wallet's color/emoji
            Surface(
                shape = CircleShape,
                color = Color(android.graphics.Color.parseColor(wallet.colorHex ?: "#CCCCCC")),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = wallet.iconEmoji ?: "💰")
                }
            }
        },
        headlineContent = {
            Text(
                text = wallet.name,
                style = AppTypography.titleMedium,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
        },
        supportingContent = {
            // Assuming UiFormatters.formatAddress is available
            Text(
                text = UiFormatters.formatAddress(wallet.publicKey),
                style = AppTypography.bodySmall,
                color = AppColors.TextSecondary
            )
        },
        trailingContent = {
            if (isActive) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = "Active Wallet",
                    tint = AppColors.Success
                )
            }
        },
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        )
    )
}