package com.decagon.domain.repository

import com.decagon.domain.model.DecagonTransaction
import kotlinx.coroutines.flow.Flow

interface DecagonTransactionRepository {
    suspend fun insertTransaction(tx: DecagonTransaction)
    suspend fun insertPendingTransaction(tx: DecagonTransaction)
    suspend fun deletePendingTransaction(txId: String)
    fun getPendingTransactions(walletId: String): Flow<List<DecagonTransaction>>
    fun getTransactionHistory(address: String): Flow<List<DecagonTransaction>>
    suspend fun updateTransactionStatus(txId: String, signature: String, status: String)

    // ✅ NEW: Get single transaction by ID
    fun getTransactionById(txId: String): Flow<DecagonTransaction?>

    // ✅ NEW: Get recent transactions (limit 50)
    fun getRecentTransactions(): Flow<List<DecagonTransaction>>
}