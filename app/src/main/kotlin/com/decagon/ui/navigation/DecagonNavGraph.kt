package com.decagon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.decagon.core.network.NetworkEnvironment
import com.decagon.core.network.NetworkManager
import com.decagon.core.util.DecagonLoadingState
import com.decagon.ui.screen.chains.DecagonSupportedChainsScreen
import com.decagon.ui.screen.create.DecagonCreateWalletScreen
import com.decagon.ui.screen.history.DecagonTransactionDetailScreen
import com.decagon.ui.screen.history.DecagonTransactionHistoryScreen
import com.decagon.ui.screen.imports.DecagonImportWalletScreen
import com.decagon.ui.screen.imports.DecagonWalletChoiceScreen
import com.decagon.ui.screen.onramp.DecagonOnRampScreen
import com.decagon.ui.screen.settings.DecagonRevealPrivateKeyScreen
import com.decagon.ui.screen.settings.DecagonRevealRecoveryScreen
import com.decagon.ui.screen.settings.DecagonSettingsScreen
import com.decagon.ui.screen.swap.SwapScreen
import com.decagon.ui.screen.swap.SwapViewModel
import com.decagon.ui.screen.wallet.DecagonWalletScreen
import com.decagon.ui.screen.wallet.DecagonWalletViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
fun DecagonNavGraph(
    startDestination: DecagonRoute = DecagonRoute.Onboarding,
) {
    val navController = rememberNavController()
    Timber.d("NavGraph initialized with start: $startDestination")

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ========== ONBOARDING ==========
        composable<DecagonRoute.Onboarding> {
            DecagonWalletChoiceScreen(
                onCreateWallet = { navController.navigate(DecagonRoute.CreateWallet) },
                onImportWallet = { navController.navigate(DecagonRoute.ImportWallet) }
            )
        }

        composable<DecagonRoute.CreateWallet> {
            DecagonCreateWalletScreen(
                onBackClick = { navController.popBackStack() },
                onWalletCreated = { _, _ ->
                    navController.navigate(DecagonRoute.Portfolio) {
                        popUpTo<DecagonRoute.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        composable<DecagonRoute.ImportWallet> {
            DecagonImportWalletScreen(
                onBackClick = { navController.popBackStack() },
                onWalletImported = {
                    navController.navigate(DecagonRoute.Portfolio) {
                        popUpTo<DecagonRoute.Onboarding> { inclusive = true }
                    }
                }
            )
        }

        // ========== MAIN TABS ==========
        composable<DecagonRoute.Portfolio> {
            val walletViewModel: DecagonWalletViewModel = koinViewModel()

            DecagonWalletScreen(
                onNavigateToOnboarding = { navController.navigate(DecagonRoute.Onboarding) },
                onNavigateToSettings = { walletId ->
                    navController.navigate(DecagonRoute.WalletSettings(walletId))
                },
                onNavigateToHistory = { navController.navigate(DecagonRoute.Activity) },
                onNavigateToBuy = { navController.navigate(DecagonRoute.Buy) },
                onNavigateToSwap = { navController.navigate(DecagonRoute.Swap) }
            )
        }

        composable<DecagonRoute.Activity> {
            DecagonTransactionHistoryScreen(
                onBackClick = { navController.popBackStack() },
                onTransactionClick = { txId ->
                    navController.navigate(DecagonRoute.TransactionDetail(txId))
                }
            )
        }

        composable<DecagonRoute.Swap> {
            val swapViewModel: SwapViewModel = koinViewModel()
            val context = LocalContext.current

            LaunchedEffect(Unit) {
                (context as? FragmentActivity)?.let { swapViewModel.setActivity(it) }
            }

            SwapScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = swapViewModel
            )
        }

        // ========== DETAIL SCREENS ==========
        composable<DecagonRoute.TransactionDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<DecagonRoute.TransactionDetail>()

            DecagonTransactionDetailScreen(
                transactionId = route.txId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // ========== ACTIONS ==========
        composable<DecagonRoute.Buy> {
            val networkManager: NetworkManager = koinInject()
            val currentNetwork by networkManager.currentNetwork.collectAsState()

            if (currentNetwork != NetworkEnvironment.MAINNET) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { navController.popBackStack() },
                    title = { androidx.compose.material3.Text("Mainnet Required") },
                    text = {
                        androidx.compose.material3.Text(
                            "Buying crypto is only available on Mainnet."
                        )
                    },
                    confirmButton = {
                        androidx.compose.material3.TextButton(
                            onClick = { navController.popBackStack() }
                        ) {
                            androidx.compose.material3.Text("OK")
                        }
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
                                navController.navigate(DecagonRoute.Portfolio) {
                                    popUpTo<DecagonRoute.Portfolio> { inclusive = true }
                                }
                            }
                        )
                    }
                    else -> {}
                }
            }
        }

        // ========== SETTINGS ==========
        composable<DecagonRoute.WalletSettings> { backStackEntry ->
            val route = backStackEntry.toRoute<DecagonRoute.WalletSettings>()
            val walletViewModel: DecagonWalletViewModel = koinViewModel()
            val walletState by walletViewModel.walletState.collectAsState()

            when (val state = walletState) {
                is DecagonLoadingState.Success -> {
                    DecagonSettingsScreen(
                        wallet = state.data,
                        onBackClick = { navController.popBackStack() },
                        onShowRecoveryPhrase = {
                            navController.navigate(DecagonRoute.RevealRecovery(route.walletId))
                        },
                        onShowPrivateKey = {
                            navController.navigate(DecagonRoute.RevealPrivateKey(route.walletId))
                        },
                        onNavigateToChains = {
                            navController.navigate(DecagonRoute.ManageChains(route.walletId))
                        }
                    )
                }
                else -> {}
            }
        }

        composable<DecagonRoute.RevealRecovery> { backStackEntry ->
            val route = backStackEntry.toRoute<DecagonRoute.RevealRecovery>()

            DecagonRevealRecoveryScreen(
                walletId = route.walletId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<DecagonRoute.RevealPrivateKey> { backStackEntry ->
            val route = backStackEntry.toRoute<DecagonRoute.RevealPrivateKey>()

            DecagonRevealPrivateKeyScreen(
                walletId = route.walletId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<DecagonRoute.ManageChains> { backStackEntry ->
            val route = backStackEntry.toRoute<DecagonRoute.ManageChains>()

            DecagonSupportedChainsScreen(
                walletId = route.walletId,
                onBackClick = { navController.popBackStack() },
                onChainSelected = { navController.popBackStack() }
            )
        }
    }
}