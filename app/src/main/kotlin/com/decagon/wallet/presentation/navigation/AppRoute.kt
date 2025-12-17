package com.octane.wallet.presentation.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes using Kotlin Serialization.
 * ✅ Wallet and Activity are now separate, independent routes.
 */
@Serializable
sealed interface AppRoute {

    // Main Navigation Tabs
    @Serializable
    data object Home : AppRoute

    @Serializable
    data object Swap : AppRoute

    @Serializable
    data object Search : AppRoute

    // ✅ Separated: Wallet Management (independent screen)
    @Serializable
    data object Wallets : AppRoute

    // ✅ Separated: Activity/Transaction History (independent screen)
    @Serializable
    data object Activity : AppRoute

    // Secondary Screens
    @Serializable
    data class TokenDetails(
        val tokenId: String,
        val symbol: String
    ) : AppRoute

    @Serializable
    data class PerpDetailRoute(
        val perpSymbol: String
    ) : AppRoute

    @Serializable
    data class DAppWebViewRoute(
        val url: String,
        val title: String
    ) : AppRoute

    @Serializable
    data class Send(
        val tokenSymbol: String? = null,
        val prefilledAddress: String? = null
    ) : AppRoute

    @Serializable
    data class Receive(
        val tokenSymbol: String = "SOL"
    ) : AppRoute

    // Wallet Management
    @Serializable
    data class SeedPhraseDisplay(
        val walletId: String,
        val walletName: String,
        val seedPhrase: String
    ) : AppRoute

    @Serializable
    data object ImportWallet : AppRoute

    @Serializable
    data object CreateWallet : AppRoute

    @Serializable
    data object ManageTokens : AppRoute

    @Serializable
    data object Settings : AppRoute

    @Serializable
    data object Security : AppRoute

    @Serializable
    data object Staking : AppRoute

    @Serializable
    data class TransactionDetails(
        val txHash: String
    ) : AppRoute
}