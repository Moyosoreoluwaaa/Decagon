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

val decagonCoreModule = module {

    // Dispatchers
    single(named("IO")) { Dispatchers.IO }
    single(named("Main")) { Dispatchers.Main }
    single(named("Default")) { Dispatchers.Default }

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            DecagonDatabase::class.java,
            DecagonDatabase.DATABASE_NAME
        )
            .addMigrations(
                DecagonDatabase.MIGRATION_1_2,
                DecagonDatabase.MIGRATION_2_3 // Add new migration
            )
            .build()
    }

    single { get<DecagonDatabase>().walletDao() }
    single { get<DecagonDatabase>().pendingTxDao() } // Add
    single { get<DecagonDatabase>().transactionDao() } // Add

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