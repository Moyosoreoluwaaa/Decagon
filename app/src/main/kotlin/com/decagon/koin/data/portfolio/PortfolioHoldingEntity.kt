package com.koin.data.portfolio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_holdings")
data class PortfolioHoldingEntity(
    @PrimaryKey val coinId: String,
    val coinName: String,
    val coinSymbol: String,
    val coinImageUrl: String,
    val quantity: Double,
    val averagePurchasePrice: Double,
    val totalTransactionFees: Double,
    val lastUpdated: Long,
    val currentPrice: Double
)