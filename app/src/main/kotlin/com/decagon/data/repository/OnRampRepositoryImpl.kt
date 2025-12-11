package com.decagon.data.repository

import com.decagon.data.local.dao.OnRampDao
import com.decagon.data.local.entity.OnRampTransactionEntity
import com.decagon.domain.model.OnRampProvider
import com.decagon.domain.model.OnRampStatus
import com.decagon.domain.model.OnRampTransaction
import com.decagon.domain.repository.OnRampRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID

class OnRampRepositoryImpl(
    private val onRampDao: OnRampDao
) : OnRampRepository {

    init {
        Timber.d("OnRampRepositoryImpl initialized")
    }

    override suspend fun createOnRampTransaction(
        walletId: String,
        walletAddress: String,
        chainId: String,
        fiatAmount: Double,
        fiatCurrency: String,
        cryptoAsset: String,
        provider: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val txId = UUID.randomUUID().toString()
            val entity = OnRampTransactionEntity(
                id = txId,
                walletId = walletId,
                walletAddress = walletAddress,
                chainId = chainId,
                fiatAmount = fiatAmount,
                fiatCurrency = fiatCurrency,
                cryptoAmount = null,
                cryptoAsset = cryptoAsset,
                provider = provider,
                providerTxId = null,
                status = OnRampStatus.INITIATED.name,
                createdAt = System.currentTimeMillis()
            )

            onRampDao.insert(entity)
            Timber.i("Created on-ramp transaction: $txId")
            Result.success(txId)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create on-ramp transaction")
            Result.failure(e)
        }
    }

    override fun getOnRampTransactions(walletId: String): Flow<List<OnRampTransaction>> {
        Timber.d("Getting on-ramp transactions for wallet: $walletId")
        return onRampDao.getByWallet(walletId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getOnRampTransactionById(txId: String): Flow<OnRampTransaction?> {
        Timber.d("Getting on-ramp transaction: $txId")
        return onRampDao.getById(txId).map { it?.toDomain() }
    }

    override suspend fun updateTransactionStatus(
        txId: String,
        status: String,
        signature: String?
    ) = withContext(Dispatchers.IO) {
        Timber.d("Updating on-ramp status: $txId -> $status")
        val completedAt = if (status == OnRampStatus.COMPLETED.name) {
            System.currentTimeMillis()
        } else null

        onRampDao.updateStatus(txId, status, signature, completedAt)
    }

    override suspend fun markTransactionCompleted(
        txId: String,
        signature: String,
        actualAmount: Double
    ) = withContext(Dispatchers.IO) {
        Timber.i("Marking on-ramp completed: $txId, amount: $actualAmount")
        onRampDao.updateCryptoAmount(txId, actualAmount)
        onRampDao.updateStatus(
            txId,
            OnRampStatus.COMPLETED.name,
            signature,
            System.currentTimeMillis()
        )
    }

    override suspend fun markTransactionFailed(
        txId: String,
        error: String
    ) = withContext(Dispatchers.IO) {
        Timber.w("Marking on-ramp failed: $txId, error: $error")
        onRampDao.markFailed(txId, error)
    }

    override fun getPendingOnRampTransactions(): Flow<List<OnRampTransaction>> {
        Timber.d("Getting pending on-ramp transactions")
        return onRampDao.getPending().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    private fun OnRampTransactionEntity.toDomain() = OnRampTransaction(
        id = id,
        walletId = walletId,
        walletAddress = walletAddress,
        chainId = chainId,
        fiatAmount = fiatAmount,
        fiatCurrency = fiatCurrency,
        cryptoAmount = cryptoAmount,
        cryptoAsset = cryptoAsset,
        provider = OnRampProvider.from(provider),
        providerTxId = providerTxId,
        status = OnRampStatus.valueOf(status),
        createdAt = createdAt,
        completedAt = completedAt,
        signature = signature,
        errorMessage = errorMessage
    )
}
