package com.decagon.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface UnifiedRoute {
    // ========== ONBOARDING ==========
    @Serializable data object Onboarding : UnifiedRoute
    @Serializable data object CreateWallet : UnifiedRoute
    @Serializable data object ImportWallet : UnifiedRoute

    // ========== MAIN TABS (Bottom Nav) ==========
    @Serializable data object Wallet : UnifiedRoute
    @Serializable data object Discover : UnifiedRoute
    @Serializable data object Perps : UnifiedRoute
    @Serializable data object DApps : UnifiedRoute
    @Serializable data object Settings : UnifiedRoute
    @Serializable data object WalletSettings : UnifiedRoute

    // ========== ACTIONS ==========
    @Serializable data object Swap : UnifiedRoute
    @Serializable data class Send(
        val tokenSymbol: String? = null,
        val prefilledAddress: String? = null
    ) : UnifiedRoute
    @Serializable data class Receive(val tokenSymbol: String = "SOL") : UnifiedRoute
    @Serializable data object Buy : UnifiedRoute

    // ========== DETAILS ==========
    @Serializable data class TokenDetails(val tokenId: String, val symbol: String) : UnifiedRoute
    @Serializable data class PerpDetail(val perpSymbol: String) : UnifiedRoute
    @Serializable data class DAppBrowser(val url: String, val title: String) : UnifiedRoute
    @Serializable data class TransactionDetail(val txHash: String) : UnifiedRoute
    @Serializable data object TransactionHistory : UnifiedRoute

    // ========== WALLET-SPECIFIC SETTINGS ==========
    @Serializable data class RevealRecovery(val walletId: String) : UnifiedRoute
    @Serializable data class RevealPrivateKey(val walletId: String) : UnifiedRoute
    @Serializable data class ManageChains(val walletId: String) : UnifiedRoute
    @Serializable data object ManageTokens : UnifiedRoute
    @Serializable data object Assets : UnifiedRoute

    // ========== FULL LIST SCREENS ==========
    @Serializable data object AllTokens : UnifiedRoute
    @Serializable data object AllPerps : UnifiedRoute
    @Serializable data object AllDApps : UnifiedRoute

}