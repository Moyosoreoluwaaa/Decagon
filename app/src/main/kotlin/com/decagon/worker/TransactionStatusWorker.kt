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
        return try {
            val wallet = walletRepository.getActiveWallet().first()
                ?: return Result.success()

            // ✅ FIX: Query ONLY pending transactions
            val pendingTxs = transactionRepository
                .getPendingTransactionsByAddress(wallet.address)  // NEW method
                .first()

            Timber.i("Syncing ${pendingTxs.size} pending transactions")

            pendingTxs.forEach { tx ->
                tx.signature?.let { signature ->
                    try {
                        val statusResult = rpcClient.getTransactionStatus(signature)

                        statusResult.onSuccess { rpcStatus ->
                            // ✅ FIX: More robust status mapping
                            val newStatus = when (rpcStatus.lowercase()) {
                                "confirmed", "finalized" -> "CONFIRMED"
                                "failed", "error" -> "FAILED"
                                else -> return@onSuccess  // Skip if still pending
                            }

                            transactionRepository.updateTransactionStatus(
                                txId = tx.id,
                                signature = signature,
                                status = newStatus
                            )

                            Timber.i("✅ Updated ${tx.id}: $rpcStatus → $newStatus")
                        }

                        statusResult.onFailure { error ->
                            Timber.w(error, "⚠️ Failed to check ${tx.id}: ${error.message}")
                        }

                    } catch (e: Exception) {
                        Timber.e(e, "❌ Error checking ${tx.id}")
                    }
                } ?: Timber.w("⚠️ Transaction ${tx.id} has no signature")
            }

            Timber.d("📊 Debug Info:")
            Timber.d("   Wallet: ${wallet.address.take(8)}...")
            Timber.d("   Total pending: ${pendingTxs.size}")
            pendingTxs.forEach { tx ->
                Timber.d("   - ${tx.id}: sig=${tx.signature?.take(8)} status=${tx.status}")
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Worker failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}