package com.decagon.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.decagon.data.local.entity.PendingTxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTxDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: PendingTxEntity)
    
    @Query("SELECT * FROM pending_transactions ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PendingTxEntity>>
    
    @Query("SELECT * FROM pending_transactions WHERE fromWalletId = :walletId")
    fun getByWallet(walletId: String): Flow<List<PendingTxEntity>>
    
    @Delete
    suspend fun delete(tx: PendingTxEntity)
    
    @Query("UPDATE pending_transactions SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetryCount(id: String)
}
