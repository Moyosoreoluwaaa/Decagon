package com.decagon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.decagon.data.local.entity.OnRampTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OnRampDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: OnRampTransactionEntity)

    @Query("SELECT * FROM onramp_transactions WHERE walletId = :walletId ORDER BY createdAt DESC")
    fun getByWallet(walletId: String): Flow<List<OnRampTransactionEntity>>

    @Query("SELECT * FROM onramp_transactions WHERE id = :txId")
    fun getById(txId: String): Flow<OnRampTransactionEntity?>

    @Query("SELECT * FROM onramp_transactions WHERE status = 'PENDING' OR status = 'INITIATED'")
    fun getPending(): Flow<List<OnRampTransactionEntity>>

    @Query("UPDATE onramp_transactions SET status = :status, signature = :signature, completedAt = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, signature: String?, completedAt: Long?)

    @Query("UPDATE onramp_transactions SET cryptoAmount = :amount WHERE id = :id")
    suspend fun updateCryptoAmount(id: String, amount: Double)

    @Query("UPDATE onramp_transactions SET errorMessage = :error, status = 'FAILED' WHERE id = :id")
    suspend fun markFailed(id: String, error: String)
}
