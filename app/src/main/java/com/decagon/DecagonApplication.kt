package com.decagon

import android.app.Application
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.decagon.di.unifiedAppModule
import com.decagon.worker.BalanceSyncManager
import com.decagon.worker.TransactionCleanupWorker
import com.decagon.worker.TransactionHistoryWorker
import com.decagon.worker.TransactionSyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import java.util.concurrent.TimeUnit

class DecagonApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        BalanceSyncManager(this).schedulePeriodicSync()
        TransactionSyncManager(this).schedulePeriodic()

        // ✅ ADD THIS: Schedule stuck transaction cleanup
        scheduleTransactionCleanup()

        // Schedule periodic transaction history sync
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<TransactionHistoryWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "transaction_history_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.WARNING else Level.ERROR)
            androidContext(this@DecagonApplication)
//            workManagerFactory()
            modules(unifiedAppModule)
        }
        Timber.d("Koin initialized with all modules including on-ramp")
    }

    // ✅ ADD THIS METHOD
    private fun scheduleTransactionCleanup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val cleanupRequest = PeriodicWorkRequestBuilder<TransactionCleanupWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "transaction_cleanup",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )

        Timber.i("Transaction cleanup scheduled (every 15 minutes)")
    }
}