package com.decagon.di

import androidx.room.Room
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.core.crypto.DecagonSecureEnclaveManager
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.data.local.DecagonDatabase
import com.decagon.data.repository.DecagonOnboardingStateRepository
import com.decagon.data.repository.DecagonOnboardingStateRepositoryImpl
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Decagon Core Module - Updated for Swap Feature
 *
 * CHANGES:
 * - Added MIGRATION_7_8 to Room builder
 * - Registered swapHistoryDao()
 * - Registered cachedTokenDao()
 */
val decagonCoreModule = module {

    // Dispatchers
    single(named("IO")) { Dispatchers.IO }
    single(named("Main")) { Dispatchers.Main }
    single(named("Default")) { Dispatchers.Default }

    // Database with NEW migration
    single {
        Room.databaseBuilder(
            androidContext(),
            DecagonDatabase::class.java,
            DecagonDatabase.DATABASE_NAME
        )
            .addMigrations(
                DecagonDatabase.MIGRATION_1_2,
                DecagonDatabase.MIGRATION_2_3,
                DecagonDatabase.MIGRATION_3_4,
                DecagonDatabase.MIGRATION_4_5,
                DecagonDatabase.MIGRATION_5_6,
                DecagonDatabase.MIGRATION_6_7,
                DecagonDatabase.MIGRATION_7_8  // ← NEW: Swap tables migration
            )
            .build()
    }

    // Existing DAOs
    single { get<DecagonDatabase>().walletDao() }
    single { get<DecagonDatabase>().pendingTxDao() }
    single { get<DecagonDatabase>().transactionDao() }
    single { get<DecagonDatabase>().onRampDao() }

    // NEW: Swap feature DAOs
    single { get<DecagonDatabase>().swapHistoryDao() }
    single { get<DecagonDatabase>().cachedTokenDao() }

    // Onboarding state repository
    single<DecagonOnboardingStateRepository> {
        DecagonOnboardingStateRepositoryImpl(get())
    }

    // Crypto utilities
    single { DecagonMnemonic() }
    single { DecagonKeyDerivation() }
    single { DecagonSecureEnclaveManager(androidContext()) }

    // Security
    single { DecagonBiometricAuthenticator(androidContext()) }
}