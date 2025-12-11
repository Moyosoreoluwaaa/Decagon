package com.decagon.domain.repository

import com.decagon.domain.model.OnRampTransaction
import kotlinx.coroutines.flow.Flow

interface OnRampRepository {

    suspend fun createOnRampTransaction(
        walletId: String,
        walletAddress: String,
        chainId: String,
        fiatAmount: Double,
        fiatCurrency: String,
        cryptoAsset: String,
        provider: String
    ): Result<String> // Returns transaction ID

    fun getOnRampTransactions(walletId: String): Flow<List<OnRampTransaction>>

    fun getOnRampTransactionById(txId: String): Flow<OnRampTransaction?>

    suspend fun updateTransactionStatus(
        txId: String,
        status: String,
        signature: String? = null
    )

    suspend fun markTransactionCompleted(
        txId: String,
        signature: String,
        actualAmount: Double
    )

    suspend fun markTransactionFailed(txId: String, error: String)

    fun getPendingOnRampTransactions(): Flow<List<OnRampTransaction>>
}
