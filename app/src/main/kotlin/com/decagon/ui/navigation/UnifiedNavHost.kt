package com.decagon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.decagon.core.network.NetworkManager
import com.decagon.ui.screen.all.AllDAppsScreen
import com.decagon.ui.screen.all.AllPerpsScreen
import com.decagon.ui.screen.all.AllTokensScreen
import com.decagon.ui.screen.assets.AssetsScreen
import com.decagon.ui.screen.chains.DecagonSupportedChainsScreen
import com.decagon.ui.screen.create.DecagonCreateWalletScreen
import com.decagon.ui.screen.dapp.DAppBrowserScreen
import com.decagon.ui.screen.discover.DiscoverScreen
import com.decagon.ui.screen.history.DecagonTransactionDetailScreen
import com.decagon.ui.screen.history.DecagonTransactionHistoryScreen
import com.decagon.ui.screen.imports.DecagonImportWalletScreen
import com.decagon.ui.screen.imports.DecagonWalletChoiceScreen
import com.decagon.ui.screen.perps.PerpDetailScreen
import com.decagon.ui.screen.settings.DecagonRevealPrivateKeyScreen
import com.decagon.ui.screen.settings.DecagonRevealRecoveryScreen
import com.decagon.ui.screen.settings.UnifiedSettingsScreen
import com.decagon.ui.screen.settings.WalletSettingsScreen
import com.decagon.ui.screen.swap.DecagonSwapScreen
import com.decagon.ui.screen.swap.SwapViewModel
import com.decagon.ui.screen.token.TokenDetailsScreen
import com.decagon.ui.screen.wallet.DecagonWalletScreen
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun UnifiedNavHost(
    startDestination: UnifiedRoute = UnifiedRoute.Onboarding
) {
    val navController = rememberNavController()
    val hapticFeedback = LocalHapticFeedback.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        // ========== ONBOARDING ==========
        composable<UnifiedRoute.Onboarding> {
            DecagonWalletChoiceScreen(
                onCreateWallet = {
                    navController.navigate(UnifiedRoute.CreateWallet)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onImportWallet = {
                    navController.navigate(UnifiedRoute.ImportWallet)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )
        }

        composable<UnifiedRoute.CreateWallet> {
            DecagonCreateWalletScreen(
                onBackClick = {
                    navController.popBackStack()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onWalletCreated = { _, _ ->
                    navController.navigate(UnifiedRoute.Wallet) {
                        popUpTo<UnifiedRoute.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<UnifiedRoute.ImportWallet> {
            DecagonImportWalletScreen(
                onBackClick = {
                    navController.popBackStack()
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onWalletImported = {
                    navController.navigate(UnifiedRoute.Wallet) {
                        popUpTo<UnifiedRoute.Onboarding> { inclusive = true }
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
            )
        }

        // ========== MAIN TABS ==========
        composable<UnifiedRoute.Wallet> {
            DecagonWalletScreen(
                onNavigateToOnboarding = {
                    navController.navigate(UnifiedRoute.Onboarding)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onNavigateToSettings = { walletId ->
                    navController.navigate(UnifiedRoute.Settings)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onNavigateToHistory = {
                    navController.navigate(UnifiedRoute.TransactionHistory)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onNavigateToBuy = {
                    navController.navigate(UnifiedRoute.Buy)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onNavigateToSwap = {
                    navController.navigate(UnifiedRoute.Swap)
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                navController = navController
            )
        }

        composable<UnifiedRoute.Assets> {
            AssetsScreen(navController = navController)
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
                onNavigateToAllTokens = {
                    navController.navigate(UnifiedRoute.AllTokens)
                },
                onNavigateToAllPerps = {
                    navController.navigate(UnifiedRoute.AllPerps)
                },
                onNavigateToAllDApps = {
                    navController.navigate(UnifiedRoute.AllDApps)
                }
            )
        }

        // ========== FULL LIST SCREENS ==========
        composable<UnifiedRoute.AllTokens> {
            AllTokensScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToTokenDetails = { id, symbol ->
                    navController.navigate(UnifiedRoute.TokenDetails(id, symbol))
                }
            )
        }

        composable<UnifiedRoute.AllPerps> {
            AllPerpsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToPerpDetails = { symbol ->
                    navController.navigate(UnifiedRoute.PerpDetail(symbol))
                }
            )
        }

        composable<UnifiedRoute.AllDApps> {
            AllDAppsScreen(
                onNavigateBack = { navController.navigateUp() },
                onNavigateToDAppBrowser = { url, title ->
                    navController.navigate(UnifiedRoute.DAppBrowser(url, title))
                }
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
        }

        // ========== SETTINGS ==========
        composable<UnifiedRoute.Settings> {
            UnifiedSettingsScreen(navController = navController)
        }

        // ✅ WALLET SETTINGS (WALLET-SPECIFIC)
        composable<UnifiedRoute.WalletSettings> {
            WalletSettingsScreen(navController = navController)
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
