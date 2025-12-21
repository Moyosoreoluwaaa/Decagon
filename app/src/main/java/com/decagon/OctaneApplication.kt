package com.decagon

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import com.octane.wallet.core.di.coreModule
import com.octane.wallet.data.di.dataModule
import com.octane.wallet.data.di.preferencesModule
import com.octane.wallet.data.di.repositoryModule
import com.octane.wallet.domain.di.domainModule
import com.wallet.di.networkModule
import com.wallet.di.viewModelModule
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import java.util.concurrent.TimeUnit

class OctaneApplication : Application(), SingletonImageLoader.Factory {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Initialize Koin DI
        startKoin {
            // Logging: ERROR in production, DEBUG in development
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)

            // Provide Android Context to modules
            androidContext(this@OctaneApplication)

            // Load modules IN ORDER (dependencies first)
            modules(
                // Layer 0: Core Infrastructure (no dependencies)
                coreModule,        // NetworkMonitor, LoadingState, Analytics, etc.
                networkModule,     // Retrofit, OkHttp, Ktor clients

                // Layer 1: Data Sources (depend on Core)
                dataModule,        // Room Database, API services
                preferencesModule, // ✅ UserPreferencesStore (DataStore)

                // Layer 2: Domain Logic (depend on Data)
                repositoryModule,  // Repository implementations
                domainModule,      // Use cases

                // Layer 3: Presentation (depend on Domain)
                viewModelModule    // ViewModels
            )
        }
    }

    // ⭐ FIXED: Coil 3.x proper configuration ⭐
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .crossfade(true)

            // ✅ FIX 1: Memory cache configuration
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25) // Use 25% of available RAM
                    .build()
            }

            // ✅ FIX 2: Disk cache with okio.Path (not java.io.File)
            .diskCache {
                DiskCache.Builder()
                    .directory(
                        context.cacheDir.resolve("image_cache").toOkioPath()
                    ) // ⭐ Convert to okio.Path
                    .maxSizeBytes(50 * 1024 * 1024) // 50 MB disk cache
                    .build()
            }

            // ✅ FIX 3: Use OkHttpNetworkFetcherFactory (not okHttpClient builder)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            OkHttpClient.Builder()
                                .connectTimeout(30, TimeUnit.SECONDS)
                                .readTimeout(30, TimeUnit.SECONDS)
                                .writeTimeout(30, TimeUnit.SECONDS)
                                .build()
                        }
                    )
                )
            }

            .build()
    }
}