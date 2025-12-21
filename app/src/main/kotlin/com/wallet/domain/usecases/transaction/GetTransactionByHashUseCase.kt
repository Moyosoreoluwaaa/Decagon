package com.octane.wallet.domain.usecases.transaction

import com.octane.wallet.domain.models.Transaction
import com.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow

/**
 * Get single transaction by hash for detail view.
 * Observes changes to transaction status (pending → confirmed).
 */
class GetTransactionByHashUseCase(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(txHash: String): Flow<Transaction?> {
        return transactionRepository.observeTransactionByHash(txHash)
    }
}