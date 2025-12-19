package com.koin.data.coin

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity(tableName = "coin_chart", primaryKeys = ["coinId", "timeRange"])
data class CoinChartEntity(
    val coinId: String,
    val timeRange: String, // Use enum name as string
    val timestamp: Long, // When this data was fetched (epoch millis)
    val priceDataJson: String // JSON-encoded list of [timestamp, price] pairs
)
