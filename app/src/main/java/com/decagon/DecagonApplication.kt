package com.decagon

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.decagon.di.decagonCoreModule
import com.decagon.di.decagonNetworkModule
import com.decagon.di.decagonTransactionModule
import com.decagon.di.decagonWalletModule
import com.decagon.worker.BalanceSyncManager
import com.decagon.worker.TransactionHistoryWorker
import com.decagon.worker.TransactionSyncManager
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
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
        Timber.d("Application started, planting Timber DebugTree in debug mode.")
        BalanceSyncManager(this).schedulePeriodicSync()
        TransactionSyncManager(this).schedulePeriodic()


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
            modules(
                decagonCoreModule,
                decagonNetworkModule,      // ADD
                decagonWalletModule,
                decagonTransactionModule   // ADD
            )
        }
        Timber.d("Koin initialized with all modules.")
    }
}