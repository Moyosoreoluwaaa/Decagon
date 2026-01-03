package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching dApp data.
 */
@Entity(tableName = "discover_dapps")
data class DAppEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val logoUrl: String?,
    val category: String, // Store as string, convert to enum in mapper
    val url: String,
    val tvl: Double?,
    val volume24h: Double?,
    val users24h: Int?,
    val isVerified: Boolean,
    val chains: String, // Comma-separated chains
    val rating: Double,
    val tags: String, // Comma-separated tags
    val lastUpdated: Long
)
