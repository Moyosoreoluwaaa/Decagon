package com.decagon.data.repository

import com.decagon.data.local.dao.PendingTxDao
import com.decagon.data.local.dao.TransactionDao
import com.decagon.data.local.entity.PendingTxEntity
import com.decagon.data.local.entity.TransactionEntity
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.model.TransactionStatus
import com.decagon.domain.repository.DecagonTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class DecagonTransactionRepositoryImpl(
    private val pendingTxDao: PendingTxDao,
    private val transactionDao: TransactionDao
) : DecagonTransactionRepository {

    init {
        Timber.d("DecagonTransactionRepositoryImpl initialized")
    }

    override suspend fun insertTransaction(tx: DecagonTransaction) = withContext(Dispatchers.IO) {
        Timber.d("Inserting transaction: ${tx.id}")
        val entity = tx.toEntity()
        transactionDao.insert(entity)
        Timber.i("Transaction inserted: ${tx.id}")
    }

    override suspend fun insertPendingTransaction(tx: DecagonTransaction) = withContext(Dispatchers.IO) {
        Timber.d("Inserting pending transaction: ${tx.id}")
        val entity = PendingTxEntity(
            id = tx.id,
            fromWalletId = tx.from,
            toAddress = tx.to,
            amount = tx.amount,
            lamports = tx.lamports,
            createdAt = tx.timestamp,
            retryCount = 0
        )
        pendingTxDao.insert(entity)
        Timber.i("Pending transaction inserted: ${tx.id}")
    }

    override suspend fun deletePendingTransaction(txId: String) = withContext(Dispatchers.IO) {
        Timber.d("Deleting pending transaction: $txId")
        pendingTxDao.getAll().map { list ->
            list.find { it.id == txId }?.let { pendingTxDao.delete(it) }
        }
        Timber.i("Pending transaction deleted: $txId")
    }

    override fun getPendingTransactions(walletId: String): Flow<List<DecagonTransaction>> {
        Timber.d("Observing pending transactions for wallet: $walletId")
        return pendingTxDao.getByWallet(walletId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTransactionHistory(address: String): Flow<List<DecagonTransaction>> {
        Timber.d("Observing transaction history for address: ${address.take(4)}...")
        return transactionDao.getByAddress(address).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun updateTransactionStatus(
        txId: String,
        signature: String,
        status: String
    ) = withContext(Dispatchers.IO) {
        Timber.d("Updating transaction status: $txId -> $status")
        transactionDao.updateStatus(txId, status, signature)
        Timber.i("Transaction status updated: $txId")
    }

    // ✅ NEW: Get single transaction by ID
    override fun getTransactionById(txId: String): Flow<DecagonTransaction?> {
        Timber.d("Getting transaction by ID: $txId")
        return transactionDao.getById(txId).map { it?.toDomain() }
    }

    // ✅ NEW: Get recent transactions
    override fun getRecentTransactions(): Flow<List<DecagonTransaction>> {
        Timber.d("Getting recent transactions")
        return transactionDao.getRecent().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    // ✅ Extension: Entity to Domain
    private fun TransactionEntity.toDomain() = DecagonTransaction(
        id = id,
        from = fromAddress,
        to = toAddress,
        amount = amount,
        lamports = lamports,
        signature = signature,
        status = TransactionStatus.valueOf(status),
        timestamp = timestamp,
        fee = fee
    )

    // ✅ Extension: Domain to Entity
    private fun DecagonTransaction.toEntity() = TransactionEntity(
        id = id,
        fromAddress = from,
        toAddress = to,
        amount = amount,
        lamports = lamports,
        signature = signature,
        status = status.name,
        timestamp = timestamp,
        fee = fee
    )

    // ✅ Extension: PendingTxEntity to Domain
    private fun PendingTxEntity.toDomain() = DecagonTransaction(
        id = id,
        from = fromWalletId,
        to = toAddress,
        amount = amount,
        lamports = lamports,
        signature = null,
        status = TransactionStatus.PENDING,
        timestamp = createdAt
    )
}