package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistent swap history separate from regular transactions.
 * Tracks swap-specific metadata (slippage, price impact, routes).
 */
@Entity(tableName = "swap_history")
data class SwapHistoryEntity(
    @PrimaryKey
    val id: String,
    val walletId: String,
    val inputMint: String,
    val outputMint: String,
    val inputAmount: Double,
    val outputAmount: Double,
    val inputSymbol: String,
    val outputSymbol: String,
    val signature: String?,
    val status: String,                     // PENDING/CONFIRMED/FAILED
    val slippageBps: Int,
    val priceImpactPct: Double,
    val feeBps: Int,
    val priorityFee: Long,
    val timestamp: Long,
    val errorMessage: String? = null
)

/**
 * Token metadata cache to reduce API calls.
 * Expires after 24 hours.
 */
@Entity(tableName = "token_cache")
data class TokenCacheEntity(
    @PrimaryKey
    val address: String,                    // Mint address
    val name: String,
    val symbol: String,
    val decimals: Int,
    val logoURI: String?,
    val tags: String,                       // Comma-separated: "verified,strict"
    val dailyVolume: Double?,
    val hasFreezableAuthority: Boolean,
    val hasMintableAuthority: Boolean,
    val coingeckoId: String?,
    val cachedAt: Long                      // Timestamp for expiration
)