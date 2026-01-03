package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Token balance cache entity.
 *
 * Caches token balances for:
 * - Offline viewing
 * - Fast loading on app start
 * - Reduce API calls
 *
 * Cache Strategy:
 * - Update after every swap
 * - Update on manual refresh
 * - Expire after 5 minutes
 */
@Entity(tableName = "token_balances",
    indices = [Index(value = ["walletAddress"])])
data class TokenBalanceEntity(
    @PrimaryKey
    val id: String, // Format: "{walletAddress}:{mint}"
    val walletAddress: String,
    val mint: String,
    val amount: String,              // Raw amount (e.g., "1500000" for 1.5 USDC)
    val decimals: Int,
    val uiAmount: Double,            // Human-readable (e.g., 1.5)
    val symbol: String,              // "USDC", "SOL"
    val name: String,                // "USD Coin", "Solana"
    val logoUrl: String?,            // For UI display
    val isNative: Boolean,           // true for SOL
    val tokenAccount: String?,       // Associated token account address
    val valueUsd: Double,            // USD value (if available)
    val change24h: Double?,          // 24h price change %
    val lastUpdated: Long            // Timestamp for staleness check
) {
    companion object {
        /**
         * Generate composite key.
         */
        fun generateId(walletAddress: String, mint: String): String {
            return "$walletAddress:$mint"
        }

        /**
         * Check if balance is stale (> 5 minutes old).
         */
        fun isStale(entity: TokenBalanceEntity, maxAgeMs: Long = 300_000): Boolean {
            return System.currentTimeMillis() - entity.lastUpdated > maxAgeMs
        }
    }
}