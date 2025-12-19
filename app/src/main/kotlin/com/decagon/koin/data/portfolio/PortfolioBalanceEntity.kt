package com.koin.data.portfolio

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "portfolio_balance")
data class PortfolioBalanceEntity(
    @PrimaryKey val id: Int = 1,
    val balance: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)