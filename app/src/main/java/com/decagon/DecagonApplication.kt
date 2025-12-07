package com.decagon

import android.app.Application
import com.decagon.di.decagonCoreModule
import com.decagon.di.decagonWalletModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber // 1. Import Timber

class DecagonApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 2. Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.d("Application started, planting Timber DebugTree in debug mode.")

        startKoin {
            // Adjust Koin logger to use WARN/ERROR in debug, as Timber handles general logging
            androidLogger(if (BuildConfig.DEBUG) Level.WARNING else Level.ERROR)
            androidContext(this@DecagonApplication)
            modules(
                decagonCoreModule,
                decagonWalletModule
            )
        }
        Timber.d("Koin initialized with modules.")
    }
}