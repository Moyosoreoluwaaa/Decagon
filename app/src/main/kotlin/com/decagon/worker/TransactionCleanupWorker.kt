package com.decagon.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.decagon.core.network.RpcClientFactory
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.util.TransactionDiagnostic
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import timber.log.Timber

class TransactionCleanupWorker(
    context: Context,
    params: WorkerParameters,
    private val transactionRepository: DecagonTransactionRepository,
    private val walletRepository: DecagonWalletRepository,
    private val rpcFactory: RpcClientFactory
) : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        return try {
            val activeWallet = walletRepository.getActiveWallet().first() 
                ?: return Result.success()
            
            val diagnostic = TransactionDiagnostic(
                transactionRepository = transactionRepository,
                rpcFactory = rpcFactory,
                walletRepository = walletRepository
            )
            
            val fixed = diagnostic.diagnoseAndFixPending(activeWallet.address)
            
            Timber.i("✅ Auto-fixed $fixed stuck transactions")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Transaction cleanup failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}