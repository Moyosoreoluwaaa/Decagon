package com.octane.wallet.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.octane.wallet.core.network.NetworkMonitor
import com.octane.wallet.data.local.database.dao.TransactionDao
import com.octane.wallet.data.mappers.toDomain
import com.octane.wallet.data.mappers.toEntity
import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.octane.wallet.data.remote.dto.solana.SignaturesOptions
import com.octane.wallet.data.remote.dto.solana.SignaturesParams
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Transaction repository with offline-first + pagination.
 */
class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val solanaRpcApi: SolanaRpcApi,
    private val networkMonitor: NetworkMonitor
) : TransactionRepository {

    override fun observeTransactionsPaged(walletId: String): Flow<PagingData<Transaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { transactionDao.observeTransactionsPaged(walletId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun observeRecentTransactions(walletId: String, limit: Int): Flow<List<Transaction>> {
        return transactionDao.observeRecentTransactions(walletId, limit)
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observePendingTransactions(walletId: String?): Flow<List<Transaction>> {
        return transactionDao.observePendingTransactions(walletId.toString())
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun getTransactionByHash(txHash: String): Transaction? {
        return transactionDao.getTransactionByHash(txHash)?.toDomain()
    }

    override suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransactionStatus(
        txId: String,
        status: TransactionStatus,
        confirmationCount: Int,
        errorMessage: String?
    ) {
        transactionDao.updateTransactionStatus(
            txId = txId,
            status = status.toEntity(),
            confirmationCount = confirmationCount,
            errorMessage = errorMessage
        )
    }

    override suspend fun refreshTransactionHistory(
        walletId: String,
        publicKey: String
    ) {
        if (!networkMonitor.isConnected.value) return

        try {
            val response = solanaRpcApi.getSignaturesForAddress(
                RpcRequest(
                    method = "getSignaturesForAddress",
                    params = SignaturesParams(
                        address = publicKey,
                        options = SignaturesOptions(limit = 50)
                    )
                )
            )

            if (response.error != null) {
                throw Exception(response.error.message)
            }

            // Parse and save transactions
            response.result?.forEach { signatureInfo ->
                // Skip if already in database
                if (transactionDao.getTransactionByHash(signatureInfo.signature) != null) {
                    return@forEach
                }

                // Fetch full transaction details
                val txDetails = solanaRpcApi.getTransaction(
                    RpcRequest(
                        method = "getTransaction",
                        params = listOf(signatureInfo.signature, mapOf("encoding" to "jsonParsed"))
                    )
                )

                // Parse and insert (simplified - needs full parser)
                // This is a stub - full implementation requires parsing instructions
            }
        } catch (e: Exception) {
            // Log error, don't throw (offline-first pattern)
        }
    }

    override fun observeTransactionByHash(txHash: String): Flow<Transaction?> {
        return transactionDao.observeTransactionByHash(txHash)
            .map { it?.toDomain() }
    }
}