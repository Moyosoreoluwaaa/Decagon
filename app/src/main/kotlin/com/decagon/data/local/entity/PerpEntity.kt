package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for caching perp data.
 */
@Entity(tableName = "discover_perps")
data class PerpEntity(
    @PrimaryKey
    val id: String,
    val symbol: String,
    val name: String,
    val logoUrl: String?,
    val indexPrice: Double,
    val markPrice: Double,
    val fundingRate: Double,
    val nextFundingTime: Long,
    val openInterest: Double,
    val volume24h: Double,
    val priceChange24h: Double,
    val leverage: String,
    val exchange: String,
    val lastUpdated: Long
)
