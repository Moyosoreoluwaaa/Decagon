package com.koin.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.koin.ui.auth.AuthScreen
import com.koin.ui.auth.AuthViewModel
import com.koin.ui.coindetail.CoinDetailScreen
import com.koin.ui.coindetail.CoinDetailViewModel
import com.koin.ui.coinlist.CoinListScreen
import com.koin.ui.coinlist.CoinListViewModel
import com.koin.ui.portfolio.PortfolioScreen
import com.koin.ui.portfolio.PortfolioViewModel
import com.koin.ui.portfoliodetail.PortfolioDetailScreen
import com.koin.ui.portfoliodetail.PortfolioDetailViewModel
import com.koin.ui.profile.ProfileScreen
import com.koin.ui.profile.ProfileViewModel
import com.koin.ui.profile.EditProfileScreen
import com.koin.ui.settings.SettingsScreen
import com.koin.ui.notification.NotificationScreen
import com.koin.ui.notification.NotificationDetailScreen
import com.koin.ui.splash.SplashScreen
import com.koin.ui.transactiondetail.TransactionDetailScreen
import com.koin.ui.transactionsuccess.TransactionSuccessScreen
import com.koin.ui.transactionhistory.TransactionHistoryScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    showError: (String?) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        // Splash
        composable(Screen.Splash.route) {
            SplashScreen(navController)
        }

        // Coin List
        composable(Screen.CoinList.route) {
            val viewModel: CoinListViewModel = koinViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.error) {
                state.error?.let { showError(it) }
            }

            CoinListScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onCoinClick = { coinId ->
                    navController.navigate(Screen.CoinDetail.createRoute(coinId))
                },
                navController = navController
            )
        }

        // Coin Detail
        composable(Screen.CoinDetail.route) {
            val viewModel: CoinDetailViewModel = koinViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.error) {
                state.error?.let { showError(it) }
            }

            CoinDetailScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Auth
        composable(Screen.Auth.route) {
            val viewModel: AuthViewModel = koinViewModel()
            AuthScreen(
                viewModel = viewModel,
                onRegistered = {
                    navController.navigate(Screen.Portfolio.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // Portfolio
        composable(Screen.Portfolio.route) {
            val viewModel: PortfolioViewModel = koinViewModel()
            val state by viewModel.uiState.collectAsState()
            val selectedCoin by viewModel.selectedCoin.collectAsState()

            LaunchedEffect(state.error) {
                state.error?.let { showError(it) }
            }

            PortfolioScreen(
                state = state,
                onEvent = viewModel::onEvent,
                selectedCoin = selectedCoin,
                navController = navController,
                onPortfolioCoinClick = { coinId ->
                    navController.navigate(Screen.PortfolioCoinDetail.createRoute(coinId))
                }
            )
        }

        // Portfolio Coin Detail
        composable(Screen.PortfolioCoinDetail.route) {
            val viewModel: PortfolioDetailViewModel = koinViewModel()
            val state by viewModel.uiState.collectAsState()

            LaunchedEffect(state.error) {
                state.error?.let { showError(it) }
            }

            PortfolioDetailScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() },
                navigateToTransactionSuccess = {
                    navController.navigate(Screen.TransactionSuccess.route)
                }
            )
        }

        // Profile
        composable(Screen.Profile.route) {
            val viewModel: ProfileViewModel = koinViewModel()
            ProfileScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                navController = navController
            )
        }

        // Transaction Success
        composable(Screen.TransactionSuccess.route) {
            TransactionSuccessScreen(navController)
        }

        // Transaction History
        composable(Screen.TransactionHistory.route) {
            TransactionHistoryScreen(navController)
        }

        // Transaction Detail
        composable(Screen.TransactionDetail.route) {
            TransactionDetailScreen(navController)
        }

        // Settings
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController, onLogout = {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            })
        }

        // Edit Profile
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController)
        }

        // Notification List
        composable(Screen.Notification.route) {
            NotificationScreen(navController)
        }

        // Notification Detail
        composable(Screen.NotificationDetail.route) {
            val notificationId = it.arguments?.getString("notificationId")?.toLongOrNull()
            if (notificationId != null) {
                NotificationDetailScreen(navController, notificationId = notificationId)
            }
        }
    }
}