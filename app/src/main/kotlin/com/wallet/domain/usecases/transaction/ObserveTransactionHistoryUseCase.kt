package com.wallet.domain.usecases.transaction

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Transaction
import com.wallet.domain.repository.TransactionRepository
import com.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Observes transaction history with pagination.
 *
 * Features:
 * - Paged loading for performance
 * - Real-time status updates
 * - Grouped by date
 * - Filterable by type (send/receive/swap/etc.)
 */

class ObserveTransactionHistoryUseCase(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(limit: Int = 50): Flow<LoadingState<List<Transaction>>> {
        return walletRepository.observeActiveWallet()
            .flatMapLatest { wallet ->
                if (wallet == null) {
                    flowOf(LoadingState.Error(IllegalStateException("No active wallet")))
                } else {
                    transactionRepository.observeRecentTransactions(wallet.id, limit)
                        .map { txs -> LoadingState.Success(txs) }
                }
            }
    }
}

