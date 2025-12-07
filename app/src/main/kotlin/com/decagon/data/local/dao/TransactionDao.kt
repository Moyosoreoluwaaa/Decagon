package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx:TransactionEntity)
    
    @Query("SELECT * FROM transactions WHERE fromAddress = :address OR toAddress = :address ORDER BY timestamp DESC")
    fun getByAddress(address: String): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 50")
    fun getRecent(): Flow<List<TransactionEntity>>
    
    @Query("UPDATE transactions SET status = :status, signature = :signature WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, signature: String?)
}