package com.decagon.di

import androidx.room.Room
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.core.crypto.DecagonSecureEnclaveManager
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.data.local.DecagonDatabase
import com.decagon.data.repository.DecagonOnboardingStateRepository
import com.decagon.data.repository.DecagonOnboardingStateRepositoryImpl
import com.decagon.domain.usecase.ObserveAllPerpsUseCase
import com.decagon.ui.screen.all.AllDAppsViewModel
import com.decagon.ui.screen.all.AllPerpsViewModel
import com.decagon.ui.screen.all.AllTokensViewModel
import com.koin.data.session.SessionManager
import com.koin.ui.session.SessionViewModel
import com.octane.wallet.domain.usecases.discover.ObserveDAppsByCategoryUseCase
import com.octane.wallet.domain.usecases.discover.ObserveDAppsUseCase
import com.octane.wallet.domain.usecases.discover.ObservePerpsUseCase
import com.octane.wallet.domain.usecases.discover.ObserveTokensUseCase
import com.octane.wallet.domain.usecases.discover.ObserveTrendingTokensUseCase
import com.octane.wallet.domain.usecases.discover.RefreshDAppsUseCase
import com.octane.wallet.domain.usecases.discover.RefreshPerpsUseCase
import com.octane.wallet.domain.usecases.discover.RefreshTokensUseCase
import com.octane.wallet.domain.usecases.discover.SearchDAppsUseCase
import com.octane.wallet.domain.usecases.discover.SearchPerpsUseCase
import com.octane.wallet.domain.usecases.discover.SearchTokensUseCase
import com.octane.wallet.domain.usecases.wallet.ObserveActiveWalletUseCase
import com.octane.wallet.domain.usecases.wallet.SwitchActiveWalletUseCase
import com.octane.wallet.presentation.viewmodel.DAppBrowserViewModel
import com.octane.wallet.presentation.viewmodel.DiscoverViewModel
import com.wallet.presentation.viewmodel.PerpDetailViewModel
import com.octane.wallet.presentation.viewmodel.SettingsViewModel
import com.octane.wallet.presentation.viewmodel.TokenDetailViewModel
import com.wallet.data.repository.DiscoverRepositoryImpl
import com.wallet.domain.repository.DiscoverRepository
import com.wallet.domain.usecases.discover.ObserveAllTokensUseCase
import com.wallet.domain.usecases.wallet.ObserveWalletsUseCase
import com.wallet.domain.usecases.wallet.SetActiveWalletUseCase
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
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
                DecagonDatabase.MIGRATION_2_3,
                DecagonDatabase.MIGRATION_3_4,
                DecagonDatabase.MIGRATION_4_5,
                DecagonDatabase.MIGRATION_5_6,
                DecagonDatabase.MIGRATION_6_7,
                DecagonDatabase.MIGRATION_7_8// ✅ ADD NEW MIGRATION
            )
            .build()
    }

    single { get<DecagonDatabase>().walletDao() }
    single { get<DecagonDatabase>().pendingTxDao() }
    single { get<DecagonDatabase>().transactionDao() }
    single { get<DecagonDatabase>().onRampDao() } // ✅ ADD
    single { get<DecagonDatabase>().swapHistoryDao() }
    single { get<DecagonDatabase>().tokenCacheDao() }

    single<DecagonOnboardingStateRepository> {
        DecagonOnboardingStateRepositoryImpl(get())
    }

    // Crypto utilities
    single { DecagonMnemonic() }
    single { DecagonKeyDerivation() }
    single { DecagonSecureEnclaveManager(androidContext()) }

    // Security
    single { DecagonBiometricAuthenticator(androidContext()) }

    // Discover Use Cases - Tokens
    factory { ObserveTokensUseCase(get()) }
    factory { ObserveTrendingTokensUseCase(get()) }
    factory { SearchTokensUseCase(get()) }
    factory { RefreshTokensUseCase(get()) }

// Discover Use Cases - Perps
    factory { ObserveAllTokensUseCase(get()) }
    factory { ObserveAllPerpsUseCase(get()) }
    factory { ObservePerpsUseCase(get()) }
    factory { SearchPerpsUseCase(get()) }
    factory { RefreshPerpsUseCase(get()) }

// Discover Use Cases - dApps
    factory { ObserveDAppsUseCase(get()) }
    factory { ObserveDAppsByCategoryUseCase(get()) }
    factory { SearchDAppsUseCase(get()) }
    factory { RefreshDAppsUseCase(get()) }
    factory { SetActiveWalletUseCase(get()) }
    factory { SwitchActiveWalletUseCase(get(), get()) }
    factory { ObserveWalletsUseCase(get()) }
    factory { ObserveActiveWalletUseCase(get()) }

    single<DiscoverRepository> {
        DiscoverRepositoryImpl(
            discoverApi = get(),
            defiLlamaApi = get(),
            discoverDao = get(),
            networkMonitor = get(),
            driftApi = get(),
        )
    }

    // Discover/Search
    viewModel {
        DiscoverViewModel(
            observeTrendingTokensUseCase = get(),
            searchTokensUseCase = get(),
            refreshTokensUseCase = get(),
            observePerpsUseCase = get(),
            searchPerpsUseCase = get(),
            refreshPerpsUseCase = get(),
            observeDAppsUseCase = get(),
            searchDAppsUseCase = get(),
            refreshDAppsUseCase = get()
        )
    }

    viewModel {
        DAppBrowserViewModel(
            observeActiveWalletUseCase = get(),
            oObserveWalletsUseCase = get(),
            setActiveWalletUseCase = get(),
            dappPreferencesStore = get()
        )
    }

    viewModel {
        PerpDetailViewModel(get())
    }

    // ViewModels
    viewModel { AllTokensViewModel(get(), get(), get()) }
    viewModel { AllPerpsViewModel(get(), get()) }
    viewModel { AllDAppsViewModel(get(), get()) }

    // Settings
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // Token Details
    viewModel { TokenDetailViewModel(get()) }

    single {
        SessionManager(get())
    }
    viewModel {
        SessionViewModel(get())
    }

}