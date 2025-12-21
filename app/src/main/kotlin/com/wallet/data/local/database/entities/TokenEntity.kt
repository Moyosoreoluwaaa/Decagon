package com.wallet.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching token data.
 * Offline-first: Always show cached data, refresh when online.
 */
@Entity(tableName = "discover_tokens")
data class TokenEntity(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val name: String,
    val logoUrl: String?,
    val currentPrice: Double,
    val priceChange24h: Double,
    val marketCap: Double,
    val volume24h: Double,
    val rank: Int,
    val isVerified: Boolean,
    val tags: String, // Comma-separated tags
    val mintAddress: String?,
    val lastUpdated: Long // Timestamp for staleness check
)