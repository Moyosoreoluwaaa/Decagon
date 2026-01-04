package com.decagon.worker

import android.content.Context
import androidx.work.*
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

class BalanceRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {
    
    private val repository: DecagonWalletRepository by inject()
    
    override suspend fun doWork(): Result {
        return try {
            val wallet = repository.getActiveWalletCached().first()
            wallet?.let {
                repository.refreshBalance(it.id)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}