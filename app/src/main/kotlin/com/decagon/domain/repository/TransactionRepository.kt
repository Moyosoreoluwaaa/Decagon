package com.decagon.domain.repository

import androidx.paging.PagingData
import com.decagon.domain.model.Transaction
import com.decagon.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactionsPaged(walletId: String): Flow<PagingData<Transaction>>
    fun observeRecentTransactions(walletId: String, limit: Int): Flow<List<Transaction>>
    fun observePendingTransactions(walletId: String? = null): Flow<List<Transaction>>
    suspend fun getTransactionByHash(txHash: String): Transaction?
    suspend fun insertTransaction(transaction: Transaction)
    suspend fun updateTransactionStatus(
        txId: String,
        status: TransactionStatus,
        confirmationCount: Int,
        errorMessage: String?
    )
    suspend fun refreshTransactionHistory(walletId: String, publicKey: String)
    fun observeTransactionByHash(txHash: String): Flow<Transaction?>
}
