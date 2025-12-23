package com.octane.wallet.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.decagon.ui.navigation.UnifiedRoute
import com.octane.wallet.presentation.screens.ActivityScreen
import com.octane.wallet.presentation.screens.DAppBrowserScreen
import com.octane.wallet.presentation.screens.DiscoverScreen
import com.octane.wallet.presentation.screens.HomeScreen
import com.octane.wallet.presentation.screens.ImportWalletScreen
import com.octane.wallet.presentation.screens.ManageTokensScreen
import com.octane.wallet.presentation.screens.PerpDetailScreen
import com.octane.wallet.presentation.screens.ReceiveScreen
import com.octane.wallet.presentation.screens.SeedPhraseDisplayScreen
import com.octane.wallet.presentation.screens.SendScreen
import com.octane.wallet.presentation.screens.SettingsScreen
import com.octane.wallet.presentation.screens.SwapScreen
import com.octane.wallet.presentation.screens.TokenDetailsScreen
import com.octane.wallet.presentation.screens.TransactionDetailsScreen
import com.octane.wallet.presentation.screens.WalletsScreen
import com.wallet.presentation.navigation.AppRoute
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * ✅ Fully wired navigation with separated Wallet and Activity screens.
 * Each screen has clear, type-safe navigation paths.
 */
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: AppRoute = AppRoute.Home
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ========== MAIN TABS ==========

        composable<AppRoute.Home> {
            HomeScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onNavigateToDetails = { assetId, symbol ->
                    navController.navigate(AppRoute.TokenDetails(assetId, symbol))
                },
                onNavigateToSend = { symbol ->
                    navController.navigate(AppRoute.Send(tokenSymbol = symbol))
                },
                onNavigateToReceive = {
                    navController.navigate(AppRoute.Receive())
                },
                onNavigateToSwap = {
                    navController.navigate(AppRoute.Swap)
                },
                onNavigateToManage = {
                    navController.navigate(AppRoute.ManageTokens)
                },
                onNavigateToWallets = {
                    navController.navigate(AppRoute.Wallets)
                },
                onNavigateToActivity = {
                    navController.navigate(AppRoute.Activity)
                },
                onNavigateToSettings = {
                    navController.navigate(AppRoute.Settings)
                }
            )
        }

        composable<AppRoute.Wallets> {
            Timber.d("📱 [AppNavHost] Composing WalletsScreen")

            WalletsScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onBack = {
                    Timber.d("🔙 [AppNavHost] Wallets back pressed")
                    navController.popBackStack()
                },
                onNavigateToSeedPhrase = { seedPhrase, walletName, walletEmoji, walletId ->
                    Timber.d(
                        "🚀 [AppNavHost] Navigate to SeedPhrase: name=$walletName, words=${
                            seedPhrase.split(
                                " "
                            ).size
                        }"
                    )
                    navController.navigate(
                        AppRoute.SeedPhraseDisplay(
                            walletId = walletId,
                            walletName = walletName,
                            seedPhrase = seedPhrase
                        )
                    )
                    Timber.d("✅ [AppNavHost] Navigation executed")
                },
                onNavigateToImport = {
                    Timber.d("🚀 [AppNavHost] Navigate to ImportWallet")
                    navController.navigate(AppRoute.ImportWallet)
                }
            )
        }

        composable<AppRoute.SeedPhraseDisplay> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.SeedPhraseDisplay>()
            Timber.d("📱 [AppNavHost] Composing SeedPhraseDisplayScreen")
            Timber.d("🔍 [AppNavHost] Route params: walletId=${route.walletId}, name=${route.walletName}")

            SeedPhraseDisplayScreen(
                seedPhrase = route.seedPhrase,
                walletName = route.walletName,
                onConfirm = {
                    Timber.d("✅ [AppNavHost] Seed phrase confirmed, navigating to Wallets")
                    navController.navigate(AppRoute.Wallets) {
                        popUpTo(AppRoute.Wallets) { inclusive = true }
                    }
                },
                onBack = {
                    Timber.d("🔙 [AppNavHost] SeedPhrase back pressed")
                    navController.navigateUp()
                }
            )
        }

        // ✅ Separated: Activity/Transaction History Screen
        composable<AppRoute.Activity> {
            ActivityScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onBack = { navController.popBackStack() },
                onNavigateToDetails = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                }
            )
        }

        composable<AppRoute.Swap> {
            SwapScreen(
                viewModel = koinViewModel(),
                navController = navController,
                onBack = { navController.popBackStack() },
                onSuccess = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                }
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

        // ========== DETAIL SCREENS ==========

        composable<AppRoute.TokenDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.TokenDetails>()
            TokenDetailsScreen(
                tokenId = route.tokenId,
                symbol = route.symbol,
                onBack = { navController.navigateUp() },
                onNavigateToSend = { symbol ->
                    navController.navigate(AppRoute.Send(tokenSymbol = symbol))
                },
                onNavigateToReceive = { symbol ->
                    navController.navigate(AppRoute.Receive(tokenSymbol = symbol))
                },
                onNavigateToSwap = { symbol ->
                    navController.navigate(AppRoute.Swap)
                }
            )
        }

        composable<AppRoute.PerpDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.PerpDetailRoute>()
            PerpDetailScreen(
                perpSymbol = route.perpSymbol,
                onBack = { navController.navigateUp() },
                onNavigateToTrade = { perpSymbol ->
                    // Navigate to trading screen when implemented
                }
            )
        }

        composable<AppRoute.DAppWebViewRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.DAppWebViewRoute>()
            DAppBrowserScreen(
                initialUrl = route.url,
                initialTitle = route.title,
                onBack = { navController.navigateUp() }
            )
        }

        composable<AppRoute.TransactionDetails> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.TransactionDetails>()
            TransactionDetailsScreen(
                txHash = route.txHash,
                onBack = { navController.navigateUp() }
            )
        }

        // ========== TRANSACTION SCREENS ==========

        composable<AppRoute.Send> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Send>()
            SendScreen(
                viewModel = koinViewModel(),
                prefilledSymbol = route.tokenSymbol,
                prefilledAddress = route.prefilledAddress,
                onBack = { navController.popBackStack() },
                onSuccess = { txHash ->
                    navController.navigate(AppRoute.TransactionDetails(txHash))
                }
            )
        }

        composable<AppRoute.Receive> { backStackEntry ->
            val route = backStackEntry.toRoute<AppRoute.Receive>()
            ReceiveScreen(
                viewModel = koinViewModel(),
                selectedSymbol = route.tokenSymbol,
                onBack = { navController.popBackStack() }
            )
        }

        // ========== WALLET MANAGEMENT ==========

        composable<AppRoute.ImportWallet> {
            ImportWalletScreen(
                viewModel = koinViewModel(),
                onSuccess = {
                    navController.navigate(AppRoute.Wallets) {
                        popUpTo(AppRoute.Wallets) { inclusive = true }
                    }
                },
                onBack = { navController.navigateUp() }
            )
        }

        // ========== SETTINGS ==========

        composable<AppRoute.ManageTokens> {
            ManageTokensScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() }
            )
        }

        composable<AppRoute.Settings> {
            SettingsScreen(
                viewModel = koinViewModel(),
                onBack = { navController.popBackStack() },
            )
        }
    }
}