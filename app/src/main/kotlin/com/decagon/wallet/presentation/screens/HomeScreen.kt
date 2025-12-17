package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.presentation.components.BottomNavBar
import com.octane.wallet.presentation.components.ErrorScreen
import com.octane.wallet.presentation.components.HomeContentRouter
import com.octane.wallet.presentation.components.OfflineBanner
import com.octane.wallet.presentation.components.ShimmerLoadingScreen
import com.octane.wallet.presentation.components.WalletActionBottomSheet
import com.octane.wallet.presentation.viewmodel.HomeEvent
import com.octane.wallet.presentation.viewmodel.HomeViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Home screen with portfolio overview.
 * Connected to HomeViewModel for real data.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = koinViewModel(),
    navController: NavController,
    onNavigateToDetails: (String, String) -> Unit,
    onNavigateToSend: (String) -> Unit,
    onNavigateToReceive: () -> Unit,
    onNavigateToSwap: () -> Unit,
    onNavigateToManage: () -> Unit,
    onNavigateToWallets: () -> Unit,
    onNavigateToActivity: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val portfolioState by viewModel.portfolioState.collectAsState()
    val activeWallet by viewModel.activeWallet.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    // ✅ Bottom sheet state
    var showWalletActionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.NavigateToWallets -> onNavigateToWallets()
                is HomeEvent.NavigateToSend -> onNavigateToSend(event.tokenSymbol)
                HomeEvent.NavigateToReceive -> onNavigateToReceive()
                HomeEvent.NavigateToSwap -> onNavigateToSwap()
                HomeEvent.NavigateToBuy -> { /* TODO: Buy flow */
                }

                HomeEvent.NavigateToManage -> onNavigateToManage()
                is HomeEvent.NavigateToDetails -> onNavigateToDetails(event.assetId, event.symbol)
                is HomeEvent.NavigateToTransactionDetails -> {}
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) {
            when (val state = portfolioState) {
                is LoadingState.Loading -> ShimmerLoadingScreen() // ✅ Use shimmer instead of basic loading

                is LoadingState.Success -> {
                    HomeContentRouter(
                        hasWallet = activeWallet != null,
                        hasBalance = state.data.totalValueUsd > 0.0,
                        portfolioData = state.data,
                        activeWallet = activeWallet,
                        recentTransactions = recentTransactions,
                        pendingCount = pendingCount,
                        isRefreshing = isRefreshing,
                        networkStatus = networkStatus,
                        onRefresh = viewModel::onRefresh,
                        onWalletClick = onNavigateToWallets,
                        onAssetClick = viewModel::onAssetClick,
                        onQuickSend = {
                            if (activeWallet == null) showWalletActionSheet = true
                            else viewModel.onQuickSend(it)
                        },
                        onQuickReceive = {
                            if (activeWallet == null) showWalletActionSheet = true
                            else viewModel.onQuickReceive()
                        },
                        onQuickSwap = {
                            if (activeWallet == null) showWalletActionSheet = true
                            else viewModel.onQuickSwap()
                        },
                        onQuickBuy = {
                            if (activeWallet == null) showWalletActionSheet = true
                            else { /* Buy flow */
                            }
                        },
                        onManageTokens = viewModel::onManageTokens,
                        onActivityClick = onNavigateToActivity,
                        onSettingsClick = onNavigateToSettings,
                        onCreateWallet = onNavigateToWallets,
                        onImportWallet = onNavigateToWallets,
                        formatCurrency = viewModel::formatCurrency
                    )
                }

                is LoadingState.Error -> ErrorScreen(
                    message = state.message ?: "Failed to load portfolio",
                    onRetry = viewModel::onRefresh
                )

                else -> {}
            }

            if (!networkStatus.isConnected) {
                OfflineBanner(modifier = Modifier.align(Alignment.TopCenter))
            }
        }
    }

    // ✅ Wallet action bottom sheet
    if (showWalletActionSheet) {
        WalletActionBottomSheet(
            onDismiss = { showWalletActionSheet = false },
            onCreateWallet = {
                showWalletActionSheet = false
                onNavigateToWallets()
            },
            onImportWallet = {
                showWalletActionSheet = false
                onNavigateToWallets()
            }
        )
    }
}