package com.wallet.domain.usecases.transaction

import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.octane.wallet.domain.models.TransactionStatus
import com.wallet.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first

/**
 * Monitors pending transactions and updates their status.
 *
 * Runs in background:
 * - Polls RPC for transaction confirmations
 * - Updates transaction status in database
 * - Notifies user when confirmed/failed
 * - Auto-retries failed transactions (optional)
 */
class MonitorPendingTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    private val solanaRpcApi: SolanaRpcApi
) {
    suspend operator fun invoke() {
        val pendingTxs = transactionRepository
            .observePendingTransactions().first()

        pendingTxs.forEach { tx ->
            try {
                // Query transaction status
                val response = solanaRpcApi.getSignatureStatuses(
                    RpcRequest(
                        method = "getSignatureStatuses",
                        params = listOf(listOf(tx.txHash))
                    )
                )

                val status = response.result?.value?.firstOrNull()

                if (status != null) {
                    val newStatus = when {
                        status.err != null -> TransactionStatus.FAILED
                        status.confirmationStatus == "finalized" -> TransactionStatus.CONFIRMED
                        else -> TransactionStatus.PENDING
                    }

                    transactionRepository.updateTransactionStatus(
                        txId = tx.id,
                        status = newStatus,
                        confirmationCount = status.confirmations ?: 0,
                        errorMessage = status.err?.toString()
                    )
                }
            } catch (e: Exception) {
                // Log error but continue processing other transactions
                println("Error monitoring transaction ${tx.txHash}: ${e.message}")
            }
        }
    }
}
