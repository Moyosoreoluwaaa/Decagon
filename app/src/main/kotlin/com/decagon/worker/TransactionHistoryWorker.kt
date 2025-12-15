package com.decagon.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.decagon.core.network.RpcClientFactory
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.model.TransactionDetails
import com.decagon.domain.model.TransactionStatus
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.UUID

class TransactionHistoryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val transactionRepository: DecagonTransactionRepository by inject()
    private val walletRepository: DecagonWalletRepository by inject()
    private val rpcFactory: RpcClientFactory by inject()  // ← CHANGED: Factory instead of client

    init {
        Timber.d("TransactionHistoryWorker initialized with RpcClientFactory")
    }

    override suspend fun doWork(): Result {
        Timber.d("TransactionHistoryWorker started")

        return try {
            val wallet = walletRepository.getActiveWallet().first() ?: run {
                Timber.d("No active wallet, skipping history sync")
                return Result.success()
            }

            val activeChain = wallet.activeChain ?: run {
                Timber.w("No active chain selected")
                return Result.success()
            }

            Timber.d("Fetching transaction history for chain: ${activeChain.chainId}")

            // ✅ CREATE NETWORK-AWARE RPC CLIENT
            val rpcClient = rpcFactory.createSolanaClient(activeChain.chainId)
            Timber.d("RPC client created for history sync: ${activeChain.chainId}")

            // Fetch transaction signatures from RPC (on correct network)
            val signatures = rpcClient.getSignaturesForAddress(wallet.address).getOrThrow()
            Timber.i("Fetched ${signatures.size} signatures from ${activeChain.chainId}")

            // Get existing transactions from DB
            val existingTxs = transactionRepository
                .getTransactionHistory(wallet.address)
                .first()
            val existingSignatures = existingTxs.mapNotNull { it.signature }.toSet()

            // Filter new signatures
            val newSignatures = signatures.filter { it !in existingSignatures }
            Timber.i("Found ${newSignatures.size} new transactions")

            // Fetch and save each new transaction
            newSignatures.forEach { signature ->
                try {
                    val details = rpcClient.getTransaction(signature).getOrNull()
                    if (details != null) {
                        val tx = details.toDecagonTransaction(wallet.address)
                        transactionRepository.insertTransaction(tx)
                        Timber.d("Saved transaction: $signature")
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to fetch transaction: $signature")
                }
            }

            Timber.i("✅ TransactionHistoryWorker completed:")
            Timber.i("   Network: ${activeChain.chainId}")
            Timber.i("   Total signatures: ${signatures.size}")
            Timber.i("   New transactions: ${newSignatures.size}")
            Result.success()

        } catch (e: Exception) {
            Timber.e(e, "TransactionHistoryWorker failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

// Extension to convert RPC transaction details to domain model
private fun TransactionDetails.toDecagonTransaction(walletAddress: String): DecagonTransaction {
    // Parse transaction to determine direction and amount
    // This is simplified - you'll need to parse the actual instructions

    return DecagonTransaction(
        id = UUID.randomUUID().toString(),
        from = "Unknown", // Parse from transaction instructions
        to = walletAddress,
        amount = 0.0, // Parse from transaction instructions
        lamports = 0L, // Parse from transaction instructions
        signature = signature,
        status = if (status == "confirmed" || status == "finalized")
            TransactionStatus.CONFIRMED
        else
            TransactionStatus.PENDING,
        timestamp = blockTime * 1000,
        fee = fee,
        priorityFee = 0L
    )
}