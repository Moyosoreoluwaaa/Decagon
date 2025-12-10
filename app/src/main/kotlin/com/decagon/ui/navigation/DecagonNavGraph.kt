package com.decagon.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.decagon.ui.screen.chains.DecagonSupportedChainsScreen
import com.decagon.ui.screen.create.DecagonCreateWalletScreen
import com.decagon.ui.screen.history.DecagonTransactionDetailScreen
import com.decagon.ui.screen.history.DecagonTransactionHistoryScreen
import com.decagon.ui.screen.imports.DecagonImportWalletScreen
import com.decagon.ui.screen.imports.DecagonWalletChoiceScreen
import com.decagon.ui.screen.settings.DecagonRevealPrivateKeyScreen
import com.decagon.ui.screen.settings.DecagonRevealRecoveryScreen
import com.decagon.ui.screen.settings.DecagonSettingsScreen
import com.decagon.ui.screen.wallet.DecagonWalletScreen
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun DecagonNavGraph(
    startDestination: String,
) {
    val navController = rememberNavController()
    Timber.d("NavGraph initialized with start: $startDestination")

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Landing/Choice screen
        composable("choice") {
            Timber.d("NavGraph: Showing choice")
            DecagonWalletChoiceScreen(
                onCreateWallet = {
                    Timber.i("NavGraph: Navigate to create")
                    navController.navigate("create")
                },
                onImportWallet = {
                    Timber.i("NavGraph: Navigate to import")
                    navController.navigate("import")
                }
            )
        }

        // Create wallet flow
        composable("create") {
            Timber.d("NavGraph: Showing create")
            DecagonCreateWalletScreen(
                onBackClick = {
                    Timber.i("NavGraph: Back from create")
                    navController.popBackStack()
                },
                onWalletCreated = { _, _ ->
                    Timber.i("NavGraph: Wallet created, navigate to wallet")
                    navController.navigate("wallet") {
                        popUpTo("choice") { inclusive = true }
                    }
                }
            )
        }

        // Import wallet flow
        composable("import") {
            Timber.d("NavGraph: Showing import")
            DecagonImportWalletScreen(
                onBackClick = {
                    Timber.i("NavGraph: Back from import")
                    navController.popBackStack()
                },
                onWalletImported = {
                    Timber.i("NavGraph: Wallet imported, navigate to wallet")
                    navController.navigate("wallet") {
                        popUpTo("choice") { inclusive = true }
                    }
                }
            )
        }

        // Main wallet screen
        composable("wallet") {
            Timber.d("NavGraph: Showing wallet")

            // 1. Retrieve the ViewModel and its state
            val walletViewModel: com.decagon.ui.screen.wallet.DecagonWalletViewModel = koinViewModel()
            val walletState by walletViewModel.walletState.collectAsState()

            // 2. Determine the wallet ID from the successful state
            val currentWalletId = when (val state = walletState) {
                is com.decagon.core.util.DecagonLoadingState.Success -> state.data.id
                else -> null // Or handle the case where it's still loading/error
            }

            DecagonWalletScreen(
                onCreateWallet = {
                    Timber.i("NavGraph: Create from wallet")
                    navController.navigate("create")
                },
                onImportWallet = {
                    Timber.i("NavGraph: Import from wallet")
                    navController.navigate("import")
                },
                onNavigateToSettings = { walletId ->
                    Timber.i("NavGraph: Settings for $walletId")
                    navController.navigate("settings/$walletId")
                },
                onNavigateToHistory = {
                    Timber.i("NavGraph: Navigate to transaction history")
                    navController.navigate("transactions")
                }
            )
        }

        // ✅ NEW: Transaction history screen
        composable("transactions") {
            Timber.d("NavGraph: Showing transaction history")
            DecagonTransactionHistoryScreen(
                onBackClick = {
                    Timber.i("NavGraph: Back from transactions")
                    navController.popBackStack()
                },
                onTransactionClick = { txId ->
                    Timber.i("NavGraph: Navigate to transaction detail: $txId")
                    navController.navigate("transaction_detail/$txId")
                }
            )
        }

        // ✅ NEW: Transaction detail screen
        composable("transaction_detail/{txId}") { backStackEntry ->
            val txId = backStackEntry.arguments?.getString("txId")!!
            Timber.d("NavGraph: Showing transaction detail: $txId")

            DecagonTransactionDetailScreen(
                transactionId = txId,
                onBackClick = {
                    Timber.i("NavGraph: Back from transaction detail")
                    navController.popBackStack()
                }
            )
        }

        composable("chains/{walletId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId")!!
            Timber.d("NavGraph: Showing chains for $walletId")

            DecagonSupportedChainsScreen(
                walletId = walletId,
                onBackClick = { navController.popBackStack() },
                onChainSelected = { chainId ->
                    Timber.i("Chain selected: $chainId")
                    navController.popBackStack()
                }
            )
        }

        // Settings screen
        composable("settings/{walletId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId")!!
            Timber.d("NavGraph: Showing settings for $walletId")

            val walletViewModel: com.decagon.ui.screen.wallet.DecagonWalletViewModel =
                koinViewModel()
            val walletState by walletViewModel.walletState.collectAsState()

            val currentWallet = when (val state = walletState) {
                is com.decagon.core.util.DecagonLoadingState.Success -> state.data
                else -> null
            }

            if (currentWallet != null) {
                DecagonSettingsScreen(
                    wallet = currentWallet,
                    onBackClick = { navController.popBackStack() },
                    onShowRecoveryPhrase = {
                        navController.navigate("reveal_recovery/$walletId")
                    },
                    onShowPrivateKey = {
                        navController.navigate("reveal_private_key/$walletId")
                    },
                    onNavigateToChains = {  // ADD THIS PARAMETER
                        navController.navigate("chains/$walletId")
                    }
                )
            }
        }

        // Reveal recovery screen
        composable("reveal_recovery/{walletId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId")!!
            Timber.d("NavGraph: Revealing recovery for $walletId")

            DecagonRevealRecoveryScreen(
                walletId = walletId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Reveal private key screen
        composable("reveal_private_key/{walletId}") { backStackEntry ->
            val walletId = backStackEntry.arguments?.getString("walletId")!!
            Timber.d("NavGraph: Revealing private key for $walletId")

            DecagonRevealPrivateKeyScreen(
                walletId = walletId,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}