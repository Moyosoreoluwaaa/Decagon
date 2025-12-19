package com.koin.data.portfolio

import com.koin.app.notification.NotificationService
import com.koin.domain.model.Coin
import com.koin.domain.portfolio.PortfolioBalance
import com.koin.domain.portfolio.PortfolioHolding
import com.koin.domain.portfolio.PortfolioRepository
import com.koin.domain.portfolio.PortfolioTransaction
import com.koin.domain.portfolio.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class PortfolioRepositoryImpl (
    private val portfolioDao: PortfolioDao,
    private val notificationService: NotificationService
) : PortfolioRepository {

    override fun getHoldings(): Flow<List<PortfolioHolding>> {
        return portfolioDao.getHoldings().map { it.map { it.toDomain() } }
    }

    override fun getTransactionsForCoin(coinId: String): Flow<List<PortfolioTransaction>> {
        return portfolioDao.getTransactionsForCoin(coinId).map { it.map { it.toDomain() } }
    }

    override fun getBalance(): Flow<PortfolioBalance?> {
        return portfolioDao.getBalance().map { entity ->
            if (entity == null) {
                // If no balance exists, insert an initial one
                portfolioDao.insertBalance(PortfolioBalanceEntity(balance = 10000.0))
                PortfolioBalance(balance = 10000.0, lastUpdated = System.currentTimeMillis())
            } else {
                entity.toDomain()
            }
        }
    }

    override suspend fun buyCoin(coin: Coin, amount: Double) {
        val holding = portfolioDao.getHolding(coin.id)
        val price = coin.currentPrice
        val fee = 0.01 * amount * price // 1% fee

        if (holding != null) {
            val newQuantity = holding.quantity + amount
            val newAveragePrice = ((holding.averagePurchasePrice * holding.quantity) + (price * amount)) / newQuantity
            val newHolding = holding.copy(
                quantity = newQuantity,
                averagePurchasePrice = newAveragePrice,
                totalTransactionFees = holding.totalTransactionFees + fee,
                lastUpdated = System.currentTimeMillis()
            )
            portfolioDao.updateHolding(newHolding)
        } else {
            val newHolding = PortfolioHoldingEntity(
                coinId = coin.id,
                coinName = coin.name,
                coinSymbol = coin.symbol,
                coinImageUrl = coin.imageUrl,
                quantity = amount,
                averagePurchasePrice = price,
                totalTransactionFees = fee,
                lastUpdated = System.currentTimeMillis(),
                currentPrice = price
            )
            portfolioDao.insertHolding(newHolding)
        }

        val transaction = PortfolioTransactionEntity(
            id = UUID.randomUUID().toString(),
            coinId = coin.id,
            type = "BUY",
            quantity = amount,
            pricePerCoin = price,
            transactionFee = fee,
            timestamp = System.currentTimeMillis()
        )
        portfolioDao.insertTransaction(transaction)

        val balance = portfolioDao.getBalance().map { it?.balance }.first() ?: 10000.0
        val newBalance = balance - (amount * price) - fee
        portfolioDao.insertBalance(PortfolioBalanceEntity(balance = newBalance))
        notificationService.showCoinPurchaseNotification(coin.name, amount)
    }

    override suspend fun sellCoin(
        coinId: String,
        quantity: Double,
        pricePerCoin: Double
    ) {
        val holding = portfolioDao.getHolding(coinId)
        val fee = 0.01 * quantity * pricePerCoin // 1% fee for selling

        if (holding != null) {
            val newQuantity = holding.quantity - quantity
            if (newQuantity <= 0) {
                portfolioDao.deleteHolding(holding.coinId) // Remove holding if quantity is zero or less
            } else {
                val newHolding = holding.copy(
                    quantity = newQuantity,
                    totalTransactionFees = holding.totalTransactionFees + fee,
                    lastUpdated = System.currentTimeMillis()
                )
                portfolioDao.updateHolding(newHolding)
            }

            val transaction = PortfolioTransactionEntity(
                id = UUID.randomUUID().toString(),
                coinId = coinId,
                type = "SELL",
                quantity = quantity,
                pricePerCoin = pricePerCoin,
                transactionFee = fee,
                timestamp = System.currentTimeMillis()
            )
            portfolioDao.insertTransaction(transaction)

            val balance = portfolioDao.getBalance().map { it?.balance }.first() ?: 10000.0
            val newBalance = balance + (quantity * pricePerCoin) - fee
            portfolioDao.insertBalance(PortfolioBalanceEntity(balance = newBalance))
            notificationService.showCoinSoldNotification(holding.coinName, quantity)
        }
    }

    override suspend fun refreshPortfolio() {
        TODO("Not yet implemented")
    }

    override suspend fun getTransactionHistory(): List<Transaction> {
        TODO("Not yet implemented")
    }

    override suspend fun resetPortfolio() {
        TODO("Not yet implemented")
    }

    override suspend fun insertInitialBalance(amount: Double) {
        portfolioDao.insertBalance(PortfolioBalanceEntity(balance = amount))
    }
}