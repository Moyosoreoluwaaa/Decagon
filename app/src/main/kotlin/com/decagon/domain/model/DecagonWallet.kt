package com.decagon.domain.model

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
    val isActive: Boolean = false
) {
    val truncatedAddress: String
        get() = "${address.take(4)}...${address.takeLast(4)}"
}