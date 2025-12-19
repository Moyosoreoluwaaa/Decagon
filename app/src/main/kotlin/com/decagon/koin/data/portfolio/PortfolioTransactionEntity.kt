package com.koin.data.portfolio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_transactions")
data class PortfolioTransactionEntity(
    @PrimaryKey val id: String,
    val coinId: String,
    val type: String, // "BUY" or "SELL"
    val quantity: Double,
    val pricePerCoin: Double,
    val transactionFee: Double,
    val timestamp: Long
)