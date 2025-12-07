package com.decagon

import android.app.Application
import com.decagon.di.decagonCoreModule
import com.decagon.di.decagonNetworkModule
import com.decagon.di.decagonTransactionModule
import com.decagon.di.decagonWalletModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class DecagonApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("Application started, planting Timber DebugTree in debug mode.")

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