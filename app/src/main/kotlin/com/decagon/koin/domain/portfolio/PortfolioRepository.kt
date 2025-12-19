package com.koin.domain.portfolio

import com.koin.domain.model.Coin
import kotlinx.coroutines.flow.Flow

data class PortfolioBalance(
    val balance: Double,
    val lastUpdated: Long
)

data class PortfolioTransaction(
    val id: String,
    val coinId: String,
    val type: String, // "BUY" or "SELL"
    val quantity: Double,
    val pricePerCoin: Double,
    val transactionFee: Double,
    val timestamp: Long
)

interface PortfolioRepository {
    fun getHoldings(): Flow<List<PortfolioHolding>>
    fun getTransactionsForCoin(coinId: String): Flow<List<PortfolioTransaction>>
    fun getBalance(): Flow<PortfolioBalance?>
    suspend fun buyCoin(coin: Coin, amount: Double)
    suspend fun sellCoin(coinId: String, quantity: Double, pricePerCoin: Double)
    suspend fun refreshPortfolio()
    suspend fun getTransactionHistory(): List<Transaction>
    suspend fun resetPortfolio()
    suspend fun insertInitialBalance(amount: Double)
}