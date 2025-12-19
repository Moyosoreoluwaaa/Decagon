package com.koin.data.portfolio

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PortfolioDao {
    // Holdings
    @Query("SELECT * FROM portfolio_holdings")
    fun getHoldings(): Flow<List<PortfolioHoldingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: PortfolioHoldingEntity)

    @Update
    suspend fun updateHolding(holding: PortfolioHoldingEntity)

    @Query("SELECT * FROM portfolio_holdings WHERE coinId = :coinId")
    suspend fun getHolding(coinId: String): PortfolioHoldingEntity?

    @Query("DELETE FROM portfolio_holdings WHERE coinId = :coinId")
    suspend fun deleteHolding(coinId: String)

    // Transactions
    @Insert
    suspend fun insertTransaction(transaction: PortfolioTransactionEntity)

    @Query("SELECT * FROM portfolio_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<PortfolioTransactionEntity>>

    @Query("SELECT * FROM portfolio_transactions WHERE coinId = :coinId ORDER BY timestamp DESC")
    fun getTransactionsForCoin(coinId: String): Flow<List<PortfolioTransactionEntity>>

    // Balance
    @Query("SELECT * FROM portfolio_balance WHERE id = 1")
    fun getBalance(): Flow<PortfolioBalanceEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: PortfolioBalanceEntity)

    @Update
    suspend fun updateBalance(balance: PortfolioBalanceEntity)


}