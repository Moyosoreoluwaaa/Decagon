package com.decagon.worker

import android.content.Context
import androidx.work.*
import com.decagon.core.network.RpcClientFactory
import com.decagon.data.remote.CoinPriceService
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
    private val rpcFactory: RpcClientFactory by inject()  // ← CHANGED: Factory instead of client
    private val priceService: CoinPriceService by inject()

    init {
        Timber.d("BalanceSyncWorker initialized with RpcClientFactory")
    }

    override suspend fun doWork(): Result {
        Timber.d("BalanceSyncWorker started")

        return try {
            val wallet = walletRepository.getActiveWallet().first() ?: run {
                Timber.d("No active wallet, skipping balance sync")
                return Result.success()
            }

            val activeChain = wallet.activeChain ?: run {
                Timber.w("No active chain selected")
                return Result.success()
            }

            Timber.d("Syncing balance for chain: ${activeChain.chainId}")

            // ✅ CREATE NETWORK-AWARE RPC CLIENT
            val rpcClient = rpcFactory.createSolanaClient(activeChain.chainId)
            Timber.d("RPC client created for balance sync: ${activeChain.chainId}")

            // Fetch balance from correct network
            val balanceResult = rpcClient.getBalance(wallet.address)
            val balance = balanceResult.getOrNull() ?: run {
                Timber.e("Failed to fetch balance: ${balanceResult.exceptionOrNull()?.message}")
                return Result.retry()
            }

            // Fetch price
            val coinId = when (activeChain.chainType) {
                com.decagon.core.chains.ChainType.Solana -> CoinPriceService.COIN_ID_SOLANA
                com.decagon.core.chains.ChainType.Ethereum -> CoinPriceService.COIN_ID_ETHEREUM
                com.decagon.core.chains.ChainType.Polygon -> CoinPriceService.COIN_ID_POLYGON
            }

            val priceResult = priceService.getPrices(listOf(coinId), "usd")
            val price = priceResult.getOrNull()?.get(coinId) ?: 0.0

            Timber.i("✅ Balance sync complete:")
            Timber.i("   Network: ${activeChain.chainId}")
            Timber.i("   Balance: ${balance / 1_000_000_000.0} ${activeChain.chainType.name}")
            Timber.i("   Price: $$price USD")

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

        Timber.i("Balance sync scheduled (every 15 minutes)")
    }
}