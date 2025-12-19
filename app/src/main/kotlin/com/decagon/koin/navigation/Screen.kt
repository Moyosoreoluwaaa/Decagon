package com.koin.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object CoinList : Screen("coin_list")
    object CoinDetail : Screen("coin_detail/{coinId}") {
        fun createRoute(coinId: String) = "coin_detail/$coinId"
    }
    object Portfolio : Screen("portfolio")
    object Auth : Screen("auth")
    object Profile : Screen("profile")
    object PortfolioCoinDetail : Screen("portfolio_coin_detail/{coinId}") {
        fun createRoute(coinId: String) = "portfolio_coin_detail/$coinId"
    }
    object TransactionSuccess : Screen("transaction_success")
    object TransactionHistory : Screen("transaction_history")
    object TransactionDetail : Screen("transaction_detail/{transactionId}") {
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    object Notification : Screen("notification_list")
    object NotificationDetail : Screen("notification_detail/{notificationId}") {
        fun createRoute(notificationId: Long) = "notification_detail/$notificationId"
    }
}
