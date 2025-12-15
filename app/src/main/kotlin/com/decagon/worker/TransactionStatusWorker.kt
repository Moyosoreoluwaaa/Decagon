package com.decagon.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.decagon.core.network.RpcClientFactory
import com.decagon.domain.model.TransactionStatus
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber

/**
 * Background worker to sync transaction status from Solana blockchain.
 * Now network-aware: queries correct network for each transaction.
 */
class TransactionStatusWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val transactionRepository: DecagonTransactionRepository by inject()
    private val walletRepository: DecagonWalletRepository by inject()
    private val rpcFactory: RpcClientFactory by inject()  // ← CHANGED: Factory instead of client

    init {
        Timber.d("TransactionStatusWorker initialized with RpcClientFactory")
    }

    override suspend fun doWork(): Result {
        return try {
            val wallet = walletRepository.getActiveWallet().first() ?: run {
                Timber.d("No active wallet, skipping status sync")
                return Result.success()
            }

            val activeChain = wallet.activeChain ?: run {
                Timber.w("No active chain selected")
                return Result.success()
            }

            Timber.d("Syncing transaction status for chain: ${activeChain.chainId}")

            // ✅ CREATE NETWORK-AWARE RPC CLIENT
            val rpcClient = rpcFactory.createSolanaClient(activeChain.chainId)
            Timber.d("RPC client created for status sync: ${activeChain.chainId}")

            // Get ONLY pending transactions
            val pendingTxs = transactionRepository
                .getPendingTransactionsByAddress(wallet.address)
                .first()

            Timber.i("Syncing ${pendingTxs.size} pending transactions on ${activeChain.chainId}")

            pendingTxs.forEach { tx ->
                tx.signature?.let { signature ->
                    try {
                        val statusResult = rpcClient.getTransactionStatus(signature)

                        statusResult.onSuccess { rpcStatus ->
                            // Map RPC status to our enum
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

            Timber.d("📊 Status sync complete:")
            Timber.d("   Network: ${activeChain.chainId}")
            Timber.d("   Wallet: ${wallet.address.take(8)}...")
            Timber.d("   Pending checked: ${pendingTxs.size}")

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Worker failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}