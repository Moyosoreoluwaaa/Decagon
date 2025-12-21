package com.octane.wallet.data.local.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.octane.wallet.data.local.database.entities.TransactionEntity
import com.octane.wallet.data.mappers.EntityStatus
import com.octane.wallet.domain.models.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * DAO for transaction history.
 * Uses PagingSource for efficient loading of large transaction lists.
 */
@Dao
interface TransactionDao {
    
    /**
     * Observe transactions for a wallet (v0.2 history screen).
     * Returns PagingSource for infinite scroll.
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE wallet_id = :walletId 
        ORDER BY timestamp DESC
    """)
    fun observeTransactionsPaged(walletId: String): PagingSource<Int, TransactionEntity>
    
    /**
     * Observe recent transactions (for home screen preview).
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE wallet_id = :walletId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    fun observeRecentTransactions(walletId: String, limit: Int = 5): Flow<List<TransactionEntity>>
    
    /**
     * Observe pending transactions (for status updates).
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE wallet_id = :walletId 
        AND status = 'PENDING' 
        ORDER BY timestamp DESC
    """)
    fun observePendingTransactions(walletId: String): Flow<List<TransactionEntity>>
    
    /**
     * Get transaction by hash (v0.8 detail screen).
     */
    @Query("SELECT * FROM transactions WHERE tx_hash = :txHash")
    suspend fun getTransactionByHash(txHash: String): TransactionEntity?
    
    /**
     * Get transaction by ID.
     */
    @Query("SELECT * FROM transactions WHERE id = :txId")
    suspend fun getTransactionById(txId: String): TransactionEntity?
    
    /**
     * Insert or update transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    /**
     * Update transaction status (for live polling).
     */
    @Query("""
        UPDATE transactions 
        SET status = :status, 
            confirmation_count = :confirmationCount,
            error_message = :errorMessage
        WHERE id = :txId
    """)
    suspend fun updateTransactionStatus(
        txId: String,
        status: EntityStatus,
        confirmationCount: Int,
        errorMessage: String?
    )
    
    /**
     * Filter by type (v0.9 portfolio filtering).
     */
    @Query("""
        SELECT * FROM transactions 
        WHERE wallet_id = :walletId 
        AND type = :type 
        ORDER BY timestamp DESC
    """)
    fun observeTransactionsByType(
        walletId: String,
        type: TransactionType
    ): Flow<List<TransactionEntity>>
    
    /**
     * Delete old transactions (optional cleanup).
     */
    @Query("""
        DELETE FROM transactions 
        WHERE wallet_id = :walletId 
        AND timestamp < :cutoffTimestamp
    """)
    suspend fun deleteOldTransactions(walletId: String, cutoffTimestamp: Long)
    
    /**
     * Count total transactions (for empty state).
     */
    @Query("SELECT COUNT(*) FROM transactions WHERE wallet_id = :walletId")
    fun observeTransactionCount(walletId: String): Flow<Int>


    @Query("SELECT * FROM transactions WHERE tx_hash = :txHash LIMIT 1")
    fun observeTransactionByHash(txHash: String): Flow<TransactionEntity?>

}