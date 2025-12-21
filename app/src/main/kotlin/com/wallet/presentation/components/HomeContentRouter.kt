package com.octane.wallet.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Asset
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.Wallet
import com.octane.wallet.domain.usecases.asset.PortfolioState
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters
import com.wallet.presentation.viewmodel.NetworkStatus

@Composable
internal fun HomeContentRouter(
    hasWallet: Boolean,
    hasBalance: Boolean,
    portfolioData: PortfolioState,
    activeWallet: Wallet?,
    recentTransactions: LoadingState<List<Transaction>>,
    pendingCount: Int,
    isRefreshing: Boolean,
    networkStatus: NetworkStatus,
    onRefresh: () -> Unit,
    onWalletClick: () -> Unit,
    onAssetClick: (Asset) -> Unit,
    onQuickSend: (String) -> Unit,
    onQuickReceive: () -> Unit,
    onQuickSwap: () -> Unit,
    onQuickBuy: () -> Unit,
    onManageTokens: () -> Unit,
    onActivityClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
    formatCurrency: (Double) -> String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = Dimensions.Padding.standard,
            vertical = Dimensions.Spacing.large
        ),
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
    ) {
        // Header (always visible)
        item {
            HomeHeader(
                totalBalance = formatCurrency(portfolioData.totalValueUsd),
                changeAmount = formatCurrency(
                    portfolioData.totalValueUsd * (portfolioData.change24hPercent / 100.0)
                ),
                changePercent = portfolioData.change24hPercent,
                walletName = activeWallet?.name,
                // ✅ Provide value for walletIcon, using the wallet's icon property if available
                walletIcon = activeWallet?.iconEmoji, // Assuming your Wallet class has an 'icon: String?' property
                onWalletClick = onWalletClick,
                // ✅ Updated to new parameter name
                onActivityClick = onActivityClick,
                // ✅ New parameter (connect to a placeholder or a real action, like navigating to Settings)
                onSettingsClick = onSettingsClick
            )
        }

        // Quick Actions (always visible)
        item {
            QuickActionGrid(
                onSend = { onQuickSend("SOL") },
                onReceive = onQuickReceive,
                onSwap = onQuickSwap,
                onBuy = onQuickBuy
            )
        }

        // Content based on wallet/balance state
        when {
            // State 1: No wallet
            !hasWallet -> {
                item {
                    NoWalletHero(
                        onCreateWallet = onCreateWallet,
                        onImportWallet = onImportWallet
                    )
                }
            }

            // State 2: Wallet exists, zero balance
            !hasBalance -> {
                item {
                    EmptyWalletHero(
                        onBuyCash = onQuickBuy,
                        onDepositCrypto = onQuickReceive
                    )
                }
            }

            // State 3: Normal content
            else -> {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Your Assets",
                            style = AppTypography.titleLarge,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            "Manage",
                            style = AppTypography.labelLarge,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.clickable { onManageTokens() }
                        )
                    }
                }

                items(portfolioData.assets) { asset ->
                    TokenRow(
                        symbol = asset.symbol,
                        name = asset.name,
                        balance = UiFormatters.formatTokenAmount(
                            asset.balanceDouble,
                            asset.symbol,
                            maxDecimals = 4
                        ),
                        valueUsd = asset.valueUsd ?: 0.0,
                        changePercent = asset.priceChange24h ?: 0.0,
                        iconColor = getAssetColor(asset.symbol),
                        onClick = { onAssetClick(asset) }
                    )
                }

                // Recent Activity
                if (recentTransactions is LoadingState.Success && recentTransactions.data.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Recent Activity", style = AppTypography.titleLarge)
                            if (pendingCount > 0) {
                                PriceChangeBadge(changePercent = pendingCount.toDouble())
                            }
                        }
                    }

                    items(recentTransactions.data.take(3)) { transaction ->
                        TransactionPreviewRow(
                            transaction = transaction,
                            onClick = { }
                        )
                    }
                }
            }
        }
    }
}