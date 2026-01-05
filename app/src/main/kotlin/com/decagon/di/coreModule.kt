package com.decagon.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.core.crypto.DecagonSecureEnclaveManager
import com.decagon.core.crypto.DecagonSplTokenProgram
import com.decagon.core.network.NetworkManager
import com.decagon.core.network.NetworkManagerImpl
import com.decagon.core.network.RpcClientFactory
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.data.local.database.DecagonDatabase
import com.decagon.data.local.datastore.DAppPreferencesStore
import com.decagon.data.local.datastore.DAppPreferencesStoreImpl
import com.decagon.data.session.SessionManager
import com.octane.wallet.core.monitoring.PerformanceTracker
import com.decagon.core.network.NetworkMonitor
import com.decagon.core.network.NetworkMonitorImpl
import com.decagon.core.network.SolanaNetworkMonitor
import com.decagon.core.security.BiometricLockManager
import com.decagon.core.security.BiometricManager
import com.decagon.core.security.KeystoreManager
import com.decagon.core.security.MaliciousSignatureDetector
import com.decagon.core.security.MaliciousSignatureDetectorImpl
import com.decagon.core.security.PhishingBlocklist
import com.decagon.data.local.datastore.UserPreferencesStore
import com.decagon.data.local.datastore.UserPreferencesStoreImpl
import com.decagon.data.repository.TokenReceiveManager
import com.decagon.data.repository.TokenReceiveManagerImpl
import com.wallet.core.monitoring.AnalyticsLogger
import com.wallet.core.monitoring.FakeAnalyticsLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val Context.networkDataStore by preferencesDataStore(name = "network_settings")

val coreModule = module {
    // Dispatchers
    single(named("IO")) { Dispatchers.IO }
    single(named("Main")) { Dispatchers.Main }
    single(named("Default")) { Dispatchers.Default }

    // Shared Json Configuration
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true
        }
    }

    // Database & Daos
    single {
        Room.databaseBuilder(
            androidContext(),
            DecagonDatabase::class.java,
            DecagonDatabase.DATABASE_NAME
        )
            .addMigrations(
                DecagonDatabase.MIGRATION_1_2, DecagonDatabase.MIGRATION_2_3,
                DecagonDatabase.MIGRATION_3_4, DecagonDatabase.MIGRATION_4_5,
                DecagonDatabase.MIGRATION_5_6, DecagonDatabase.MIGRATION_6_7,
                DecagonDatabase.MIGRATION_7_8, DecagonDatabase.MIGRATION_8_9,
                DecagonDatabase.MIGRATION_9_10, DecagonDatabase.MIGRATION_10_11
            ).build()
    }
    single { get<DecagonDatabase>().walletDao() }
    single { get<DecagonDatabase>().assetDao() }
    single { get<DecagonDatabase>().contactDao() }
    single { get<DecagonDatabase>().stakingDao() }
    single { get<DecagonDatabase>().discoverDao() }
    single { get<DecagonDatabase>().approvalDao() }
    single { get<DecagonDatabase>().pendingTxDao() }
    single { get<DecagonDatabase>().transactionDao() }
    single { get<DecagonDatabase>().onRampDao() }
    single { get<DecagonDatabase>().swapHistoryDao() }
    single { get<DecagonDatabase>().tokenCacheDao() }
    single { get<DecagonDatabase>().tokenBalanceDao() }

    // Security & Crypto
    single { DecagonMnemonic() }
    single { DecagonKeyDerivation() }
    single { DecagonSecureEnclaveManager(androidContext()) }
    single { DecagonBiometricAuthenticator(androidContext()) }
    single { KeystoreManager(androidContext()) }
    single { BiometricManager(androidContext()) }
    single<MaliciousSignatureDetector> { MaliciousSignatureDetectorImpl() }
    single { PhishingBlocklist() }

    // Monitoring & Networking
    single<NetworkMonitor> { NetworkMonitorImpl(androidContext()) }
    single { SolanaNetworkMonitor(get()) }
    single<AnalyticsLogger> { FakeAnalyticsLogger() }
    single { PerformanceTracker(get()) }
    single<NetworkManager> {
        NetworkManagerImpl(
            dataStore = androidContext().networkDataStore
        )
    }
    // Simplified for example
    single { RpcClientFactory(get(), get()) }
    single<TokenReceiveManager> {
        TokenReceiveManagerImpl(
            jupiterApi = get(),
            tokenBalanceDao = get(),
            splTokenProgram = DecagonSplTokenProgram, // Singleton object
            rpcFactory = get()
        )
    }

    // Session & Preferences
    single<UserPreferencesStore> { UserPreferencesStoreImpl(androidContext()) }
    single<DAppPreferencesStore> { DAppPreferencesStoreImpl(androidContext()) }
    single { SessionManager(get()) }
    single { BiometricLockManager(get()) }
}