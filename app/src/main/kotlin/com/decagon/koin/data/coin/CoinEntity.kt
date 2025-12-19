package com.koin.data.coin

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "coins")
data class CoinEntity(
    @PrimaryKey val id: String,
    val name: String,
    val symbol: String,
    val imageUrl: String,
    val currentPrice: Double,
    val marketCap: Long,
    val marketCapRank: Int,
    val priceChange24h: Double,
    val priceChangePercentage24h: Double,
    val priceChangePercentage1h: Double?,
    val priceChangePercentage7d: Double?,
    val priceChangePercentage30d: Double?,
    @ColumnInfo(name = "sparkline_data")
    val sparklineData: List<Double>?,
    val high24h: Double?,
    val low24h: Double?,
    val totalVolume: Double?,
    val circulatingSupply: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val ath: Double?,
    val athDate: String?,
    val atl: Double?,
    val atlDate: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)