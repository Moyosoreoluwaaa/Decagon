package com.decagon.domain.model

import com.decagon.core.chains.ChainType

/**
 * Domain model for wallet.
 *
 * Pure Kotlin - no Android/Room dependencies.
 */
data class DecagonWallet(
    val id: String,
    val name: String,
    val publicKey: String,
    val address: String, // Base58 encoded for Solana
    val accountIndex: Int = 0,
    val balance: Double = 0.0, // SOL balance (mocked in 0.1)
    val createdAt: Long,
    val isActive: Boolean = false,
    val isViewOnly: Boolean = false, // Add this property
    val chains: List<ChainWallet> = listOf(),
    val activeChainId: String = ChainType.Solana.id
) {
    val activeChain: ChainWallet?
        get() = chains.find { it.chainId == activeChainId }

    val truncatedAddress: String
        get() = "${address.take(4)}...${address.takeLast(4)}"
}
