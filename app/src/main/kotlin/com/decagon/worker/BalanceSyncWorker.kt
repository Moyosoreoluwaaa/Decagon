package com.decagon.worker

import android.content.Context
import androidx.work.*
import com.decagon.data.remote.CoinPriceService
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BalanceSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val walletRepository: DecagonWalletRepository by inject()
    private val rpcClient: SolanaRpcClient by inject()
    private val priceService: CoinPriceService by inject()

    override suspend fun doWork(): Result {
        Timber.d("BalanceSyncWorker started")
        
        return try {
            val wallet = walletRepository.getActiveWallet().first() ?: return Result.success()
            
            // Fetch balance
            val balanceResult = rpcClient.getBalance(wallet.address)
            val balance = balanceResult.getOrNull() ?: return Result.retry()
            
            // Fetch price
            val priceResult = priceService.getPrices(
                listOf(CoinPriceService.COIN_ID_SOLANA),
                "usd"
            )
            
            Timber.i("Balance: ${balance / 1_000_000_000.0} SOL")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Balance sync failed")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

// Manager
class BalanceSyncManager(private val context: Context) {
    
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val request = PeriodicWorkRequestBuilder<BalanceSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "balance_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
        
        Timber.i("Balance sync scheduled")
    }
}