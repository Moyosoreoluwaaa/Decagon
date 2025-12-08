package com.decagon.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.model.TransactionStatus
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Background worker to sync transaction status from Solana blockchain.
 * 
 * Checks PENDING transactions and updates their status.
 */
class TransactionStatusWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val transactionRepository: DecagonTransactionRepository by inject()
    private val walletRepository: DecagonWalletRepository by inject()
    private val rpcClient: SolanaRpcClient by inject()

    override suspend fun doWork(): Result {
        Timber.d("TransactionStatusWorker started")

        return try {
            // Get active wallet
            val wallet = walletRepository.getActiveWallet().first()
                ?: return Result.success().also {
                    Timber.d("No active wallet, skipping sync")
                }

            // Get transaction history
            val transactions = transactionRepository
                .getTransactionHistory(wallet.address)
                .first()

            // Filter pending transactions
            val pendingTxs = transactions.filter { it.status == TransactionStatus.PENDING }
            
            Timber.i("Syncing status for ${pendingTxs.size} pending transactions")

            // Check status for each pending transaction
            var updatedCount = 0
            pendingTxs.forEach { tx ->
                tx.signature?.let { signature ->
                    try {
                        // Query transaction status from blockchain
                        val statusResult = rpcClient.getTransactionStatus(signature)
                        
                        statusResult.onSuccess { status ->
                            if (status != "pending") {
                                // Update in database
                                val newStatus = when (status) {
                                    "confirmed", "finalized" -> "CONFIRMED"
                                    "failed" -> "FAILED"
                                    else -> "PENDING"
                                }
                                
                                transactionRepository.updateTransactionStatus(
                                    txId = tx.id,
                                    signature = signature,
                                    status = newStatus
                                )
                                
                                Timber.i("Updated tx ${tx.id} to $newStatus")
                                updatedCount++
                            }
                        }
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to check status for tx: ${tx.id}")
                        // Continue with other transactions
                    }
                }
            }

            Timber.i("TransactionStatusWorker completed: $updatedCount/$${pendingTxs.size} updated")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "TransactionStatusWorker failed")
            
            // Retry if attempt count < 3
            if (runAttemptCount < 3) {
                Timber.d("Retrying... (attempt ${runAttemptCount + 1})")
                Result.retry()
            } else {
                Timber.e("Max retries reached, giving up")
                Result.failure()
            }
        }
    }
}