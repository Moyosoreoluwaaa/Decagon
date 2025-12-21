package com.octane.wallet.core.di

import androidx.room.Room
import com.wallet.core.monitoring.AnalyticsLogger
import com.wallet.core.monitoring.FakeAnalyticsLogger
import com.octane.wallet.core.monitoring.PerformanceTracker
import com.octane.wallet.core.network.NetworkMonitor
import com.octane.wallet.core.network.NetworkMonitorImpl
import com.octane.wallet.core.network.SolanaNetworkMonitor
import com.octane.wallet.core.security.BiometricManager
import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.core.security.MaliciousSignatureDetector
import com.octane.wallet.core.security.MaliciousSignatureDetectorImpl
import com.octane.wallet.core.security.PhishingBlocklist
import com.octane.wallet.data.local.database.MIGRATION_1_2
import com.octane.wallet.data.local.database.OctaneDatabase
import com.octane.wallet.data.local.datastore.DAppPreferencesStore
import com.octane.wallet.data.local.datastore.DAppPreferencesStoreImpl
import com.octane.wallet.data.local.datastore.UserPreferencesStore
import com.octane.wallet.data.local.datastore.UserPreferencesStoreImpl
import com.wallet.data.repository.SolanaKeyGeneratorImpl
import com.wallet.core.blockchain.SolanaKeyGenerator
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * ✅ FIXED: Core module following Phase 0 patterns
 * Provides cross-cutting utilities used by ALL layers
 */
val coreModule = module {
    // Dispatchers
    single(named("IO")) { Dispatchers.IO }
    single(named("Main")) { Dispatchers.Main }
    single(named("Default")) { Dispatchers.Default }

    // Room Database & DAOs
    single {
        Room.databaseBuilder(androidContext(), OctaneDatabase::class.java, OctaneDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration(false)
            .build()
    }
    single { get<OctaneDatabase>().walletDao() }
    single { get<OctaneDatabase>().transactionDao() }
    single { get<OctaneDatabase>().assetDao() }
    single { get<OctaneDatabase>().contactDao() }
    single { get<OctaneDatabase>().approvalDao() }
    single { get<OctaneDatabase>().stakingDao() }
    single { get<OctaneDatabase>().discoverDao() }

    // DataStore Preferences
    single<UserPreferencesStore> { UserPreferencesStoreImpl(androidContext()) }
    single<DAppPreferencesStore> { DAppPreferencesStoreImpl(androidContext()) }

    // Security & Utilities
    single<SolanaKeyGenerator> { SolanaKeyGeneratorImpl() } // Add this line
    single { KeystoreManager(androidContext()) }
    single { BiometricManager(androidContext()) }
    single<MaliciousSignatureDetector> { MaliciousSignatureDetectorImpl() }
    single { PhishingBlocklist() }
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single { SolanaNetworkMonitor(get()) }
    single<AnalyticsLogger> { FakeAnalyticsLogger() }
    single { PerformanceTracker(get()) }
}