// app/core/domain/models/Wallet.kt

package com.octane.wallet.domain.models

/**
 * Domain model for Wallet (pure Kotlin, no Android deps).
 */
data class Wallet(
    val id: String,
    val name: String,
    val publicKey: String,
    val iconEmoji: String? = null,
    val colorHex: String? = null,
    val chainId: String = "solana",
    val isActive: Boolean = false,
    val isHardwareWallet: Boolean = false,
    val hardwareDeviceName: String? = null,
    val createdAt: Long,
    val lastUpdated: Long
)
