package com.koin.data.transaction

import com.koin.data.coin.CoinDao
import com.koin.data.coin.toCoin
import com.koin.data.portfolio.PortfolioDao
import com.koin.data.portfolio.PortfolioTransactionEntity
import com.koin.domain.model.Transaction
import com.koin.domain.transaction.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl (
    private val transactionDao: PortfolioDao,
    private val coinDao: CoinDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { entity ->
                val coinEntity = coinDao.getCoinById(entity.coinId).first()
                val coin = coinEntity?.toCoin()
                Transaction(
                    id = entity.id,
                    coinId = entity.coinId,
                    type = entity.type,
                    quantity = entity.quantity,
                    pricePerCoin = entity.pricePerCoin,
                    transactionFee = entity.transactionFee,
                    timestamp = entity.timestamp,
                    coinName = coin?.name ?: "Unknown",
                    coinSymbol = coin?.symbol ?: "UNK"
                )
            }
        }
    }

    override suspend fun addTransaction(transaction: Transaction) {
        val entity = PortfolioTransactionEntity(
            id = transaction.id,
            coinId = transaction.coinId,
            type = transaction.type,
            quantity = transaction.quantity,
            pricePerCoin = transaction.pricePerCoin,
            transactionFee = transaction.transactionFee,
            timestamp = transaction.timestamp
        )
        transactionDao.insertTransaction(entity)
    }
}