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

    // ========== MAIN TABS (Bottom Nav - 3 tabs) ==========
    @Serializable
    data object Portfolio : DecagonRoute      // Wallet + Balance + Quick Actions (replaces old Portfolio)
    @Serializable
    data object Swap : DecagonRoute           // Swap screen
    @Serializable
    data object Activity : DecagonRoute       // Transaction history

    // ========== WALLET MANAGEMENT ==========
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