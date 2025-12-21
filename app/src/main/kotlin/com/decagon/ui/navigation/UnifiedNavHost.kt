package com.decagon.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.decagon.core.network.NetworkEnvironment
import com.decagon.core.network.NetworkManager
import com.decagon.core.util.DecagonLoadingState
import com.decagon.ui.screen.wallet.DecagonWalletScreen
import com.decagon.ui.screen.wallet.DecagonWalletViewModel
import com.decagon.ui.screen.swap.SwapViewModel
import com.decagon.ui.screen.create.DecagonCreateWalletScreen
import com.decagon.ui.screen.imports.DecagonImportWalletScreen
import com.decagon.ui.screen.imports.DecagonWalletChoiceScreen
import com.decagon.ui.screen.history.DecagonTransactionHistoryScreen
import com.decagon.ui.screen.history.DecagonTransactionDetailScreen
import com.decagon.ui.screen.onramp.DecagonOnRampScreen
import com.decagon.ui.screen.settings.DecagonSettingsScreen
import com.decagon.ui.screen.settings.DecagonRevealRecoveryScreen
import com.decagon.ui.screen.settings.DecagonRevealPrivateKeyScreen
import com.decagon.ui.screen.chains.DecagonSupportedChainsScreen
import com.decagon.ui.screen.swap.DecagonSwapScreen
import com.octane.wallet.presentation.screens.DiscoverScreen
import com.octane.wallet.presentation.screens.PerpDetailScreen
import com.octane.wallet.presentation.screens.DAppBrowserScreen
import com.octane.wallet.presentation.screens.TokenDetailsScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun UnifiedNavHost(
    startDestination: UnifiedRoute = UnifiedRoute.Onboarding
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // ========== ONBOARDING ==========
        composable<UnifiedRoute.Onboarding> {
            DecagonWalletChoiceScreen(
                onCreateWallet = { navController.navigate(UnifiedRoute.CreateWallet) },
                onImportWallet = { navController.navigate(UnifiedRoute.ImportWallet) }
            )
        }

        composable<UnifiedRoute.CreateWallet> {
            DecagonCreateWalletScreen(
                onBackClick = { navController.popBackStack() },
                onWalletCreated = { _, _ ->
                    navController.navigate(UnifiedRoute.Wallet) {
                        popUpTo<UnifiedRoute.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<UnifiedRoute.ImportWallet> {
            DecagonImportWalletScreen(
                onBackClick = { navController.popBackStack() },
                onWalletImported = {
                    navController.navigate(UnifiedRoute.Wallet) {
                        popUpTo<UnifiedRoute.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        // ========== MAIN TABS ==========
        composable<UnifiedRoute.Wallet> {
            DecagonWalletScreen(
                onNavigateToOnboarding = { navController.navigate(UnifiedRoute.Onboarding) },
                onNavigateToSettings = { walletId ->
                    navController.navigate(UnifiedRoute.WalletSettings(walletId))
                },
                onNavigateToHistory = { navController.navigate(UnifiedRoute.TransactionHistory) },
                onNavigateToBuy = { navController.navigate(UnifiedRoute.Buy) },
                onNavigateToSwap = { navController.navigate(UnifiedRoute.Swap) },
                navController = navController
            )
        }

        composable<UnifiedRoute.Discover> {
            DiscoverScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onNavigateToTokenDetails = { id, symbol ->
                    navController.navigate(UnifiedRoute.TokenDetails(id, symbol))
                },
                onNavigateToPerpDetails = { symbol ->
                    navController.navigate(UnifiedRoute.PerpDetail(symbol))
                },
                onNavigateToDAppDetails = { url ->
                    navController.navigate(UnifiedRoute.DAppBrowser(url, "DApp"))
                },
                onTokenArrow = {},
                onPerpArrow = {},
                onDAppArrow = {}
            )
        }

        composable<UnifiedRoute.TransactionHistory> {
            DecagonTransactionHistoryScreen(
                onBackClick = { navController.popBackStack() },
                onTransactionClick = { txId ->
                    navController.navigate(UnifiedRoute.TransactionDetail(txId))
                }
            )
        }

        composable<UnifiedRoute.Swap> {
            val swapViewModel: SwapViewModel = koinViewModel()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                (context as? FragmentActivity)?.let { swapViewModel.setActivity(it) }
            }

            DecagonSwapScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = swapViewModel,
                navController = navController
            )
        }

        // ========== DETAILS ==========
        composable<UnifiedRoute.TokenDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<UnifiedRoute.TokenDetails>()
            TokenDetailsScreen(
                tokenId = route.tokenId,
                symbol = route.symbol,
                onBack = { navController.navigateUp() },
                onNavigateToSend = { navController.navigate(UnifiedRoute.Send(it)) },
                onNavigateToReceive = { navController.navigate(UnifiedRoute.Receive(it)) },
                onNavigateToSwap = { navController.navigate(UnifiedRoute.Swap) }
            )
        }

        composable<UnifiedRoute.PerpDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<UnifiedRoute.PerpDetail>()
            PerpDetailScreen(
                perpSymbol = route.perpSymbol,
                onBack = { navController.navigateUp() },
                onNavigateToTrade = {}
            )
        }

        composable<UnifiedRoute.DAppBrowser> { backStackEntry ->
            val route = backStackEntry.toRoute<UnifiedRoute.DAppBrowser>()
            DAppBrowserScreen(
                initialUrl = route.url,
                initialTitle = route.title,
                onBack = { navController.navigateUp() }
            )
        }

        composable<UnifiedRoute.TransactionDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<UnifiedRoute.TransactionDetail>()
            DecagonTransactionDetailScreen(
                transactionId = route.txHash,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ========== ACTIONS ==========
        composable<UnifiedRoute.Buy> {
            val networkManager: NetworkManager = koinInject()
            val currentNetwork by networkManager.currentNetwork.collectAsState()

            if (currentNetwork != NetworkEnvironment.MAINNET) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { navController.popBackStack() },
                    title = { androidx.compose.material3.Text("Mainnet Required") },
                    text = { androidx.compose.material3.Text("Buying crypto is only available on Mainnet.") },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { navController.popBackStack() }
                        ) { androidx.compose.material3.Text("OK") }
                    }
                )
            } else {
                val walletViewModel: DecagonWalletViewModel = koinViewModel()
                val walletState by walletViewModel.walletState.collectAsState()

                when (val state = walletState) {
                    is DecagonLoadingState.Success -> {
                        DecagonOnRampScreen(
                            wallet = state.data,
                            onBackClick = { navController.popBackStack() },
                            onTransactionComplete = {
                                navController.navigate(UnifiedRoute.Wallet) {
                                    popUpTo<UnifiedRoute.Wallet> { inclusive = true }
                                }
                            }
                        )
                    }

                    else -> {}
                }
            }
        }

        // ========== SETTINGS ==========
        composable<UnifiedRoute.Settings> {
            val walletViewModel: DecagonWalletViewModel = koinViewModel()
            val walletState by walletViewModel.walletState.collectAsState()

            when (val state = walletState) {
                is DecagonLoadingState.Success -> {
                    DecagonSettingsScreen(
                        wallet = state.data,
                        onBackClick = { /* Bottom nav handles navigation - no back action */ },
                        onShowRecoveryPhrase = {
                            navController.navigate(UnifiedRoute.RevealRecovery(state.data.id))
                        },
                        onShowPrivateKey = {
                            navController.navigate(UnifiedRoute.RevealPrivateKey(state.data.id))
                        },
                        onNavigateToChains = {
                            navController.navigate(UnifiedRoute.ManageChains(state.data.id))
                        },
                        navController = navController
                    )
                }
                is DecagonLoadingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is DecagonLoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {}
            }
        }

        composable<UnifiedRoute.RevealRecovery> { backStackEntry ->
            val route = backStackEntry.toRoute<UnifiedRoute.RevealRecovery>()
            DecagonRevealRecoveryScreen(
                walletId = route.walletId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<UnifiedRoute.RevealPrivateKey> { backStackEntry ->
            val route = backStackEntry.toRoute<UnifiedRoute.RevealPrivateKey>()
            DecagonRevealPrivateKeyScreen(
                walletId = route.walletId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<UnifiedRoute.ManageChains> { backStackEntry ->
            val route = backStackEntry.toRoute<UnifiedRoute.ManageChains>()
            DecagonSupportedChainsScreen(
                walletId = route.walletId,
                onBackClick = { navController.popBackStack() },
                onChainSelected = { navController.popBackStack() }
            )
        }
    }
}
