package com.decagon.worker

import android.content.Context
import androidx.work.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Manager for scheduling transaction status synchronization.
 */
class TransactionSyncManager(private val context: Context) {

    companion object {
        private const val WORK_NAME = "transaction_status_sync"
        private const val SYNC_INTERVAL_MINUTES = 15L
    }

    /**
     * Schedules periodic transaction status sync.
     * Runs every 15 minutes to check pending transactions.
     */
    fun schedulePeriodic() {
        Timber.d("Scheduling periodic transaction sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<TransactionStatusWorker>(
            repeatInterval = SYNC_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already scheduled
            syncRequest
        )

        Timber.i("Periodic transaction sync scheduled (every $SYNC_INTERVAL_MINUTES min)")
    }

    /**
     * Triggers immediate one-time sync.
     * Use after sending a transaction.
     */
    fun syncNow() {
        Timber.d("Triggering immediate transaction sync")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<TransactionStatusWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueue(syncRequest)
        Timber.i("Immediate transaction sync triggered")
    }

    /**
     * Cancels all scheduled syncs.
     * Use when user logs out or disables feature.
     */
    fun cancelAll() {
        Timber.d("Cancelling all transaction syncs")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        Timber.i("Transaction syncs cancelled")
    }
}