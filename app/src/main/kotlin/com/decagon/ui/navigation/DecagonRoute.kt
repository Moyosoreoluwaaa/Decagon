package com.decagon.ui.navigation

import kotlinx.serialization.Serializable

sealed interface DecagonRoute {
    // ========== ONBOARDING ==========
    @Serializable
    data object Splash : DecagonRoute
    @Serializable
    data object Onboarding : DecagonRoute
    @Serializable
    data object CreateWallet : DecagonRoute
    @Serializable
    data object ImportWallet : DecagonRoute

    // ========== MAIN TABS (Bottom Nav - 4 tabs) ==========
    @Serializable
    data object Portfolio : DecagonRoute      // Wallet + Balance + Quick Actions
    @Serializable
    data object Discover : DecagonRoute       // ✅ Tokens + Perps + DApps (from Octane)
    @Serializable
    data object Swap : DecagonRoute           // Existing swap screen
    @Serializable
    data object Activity : DecagonRoute       // Transaction history

    // ========== WALLET MANAGEMENT ==========
    @Serializable
    data object Wallets : DecagonRoute        // Keep as separate full screen
    @Serializable
    data class WalletSettings(val walletId: String) : DecagonRoute
    @Serializable
    data class SeedPhraseDisplay(
        val walletId: String,
        val walletName: String,
        val seedPhrase: String
    ) : DecagonRoute

    // ========== DETAIL SCREENS ==========
    @Serializable
    data class TokenDetail(val tokenId: String, val symbol: String) : DecagonRoute
    @Serializable
    data class PerpDetail(val perpSymbol: String) : DecagonRoute
    @Serializable
    data class DAppBrowser(val url: String, val title: String) : DecagonRoute
    @Serializable
    data class TransactionDetail(val txId: String) : DecagonRoute

    // ========== ACTIONS ==========
    @Serializable
    data class Send(val tokenSymbol: String? = null, val prefilledAddress: String? = null) :
        DecagonRoute

    @Serializable
    data class Receive(val tokenSymbol: String = "SOL") : DecagonRoute
    @Serializable
    data object Buy : DecagonRoute // OnRamp

    // ========== SETTINGS ==========
    @Serializable
    data class RevealRecovery(val walletId: String) : DecagonRoute
    @Serializable
    data class RevealPrivateKey(val walletId: String) : DecagonRoute
    @Serializable
    data class ManageChains(val walletId: String) : DecagonRoute
}