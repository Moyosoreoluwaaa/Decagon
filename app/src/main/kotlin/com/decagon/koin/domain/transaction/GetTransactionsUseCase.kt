package com.koin.domain.transaction

import com.koin.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

class GetTransactionsUseCase (
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return repository.getAllTransactions()
    }
}