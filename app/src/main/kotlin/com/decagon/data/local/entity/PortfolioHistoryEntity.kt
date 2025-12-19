package com.decagon.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_history")
data class PortfolioHistoryEntity(
    @PrimaryKey val timestamp: Long,
    val totalValueUsd: Double,
    val walletId: String,
    val chainId: String
)
