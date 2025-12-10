package com.decagon.di

import com.decagon.data.repository.DecagonSettingsRepositoryImpl
import com.decagon.data.repository.DecagonWalletRepositoryImpl
import com.decagon.domain.repository.DecagonSettingsRepository
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.usecase.DecagonCreateWalletUseCase
import com.decagon.domain.usecase.DecagonImportWalletUseCase
import com.decagon.ui.screen.chains.DecagonSupportedChainsViewModel
import com.decagon.ui.screen.onboarding.DecagonOnboardingViewModel
import com.decagon.ui.screen.settings.DecagonSettingsViewModel
import com.decagon.ui.screen.wallet.DecagonWalletViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val decagonWalletModule = module {
    
    // Repository
    single<DecagonWalletRepository> {
        DecagonWalletRepositoryImpl(
            walletDao = get(),
            enclaveManager = get(),
            mnemonicHelper = get(),
            keyDerivation = get(),
            biometricAuthenticator = get()
        )
    }

    // Settings Repository
    single<DecagonSettingsRepository> {
        DecagonSettingsRepositoryImpl(
            walletDao = get(),
            enclaveManager = get(),
//            mnemonicHelper = get(),
            keyDerivation = get(),
            biometricAuthenticator = get()
        )
    }
    
    // UseCases
    factory { DecagonCreateWalletUseCase(get(), get()) }
    factory { DecagonImportWalletUseCase(get(), get()) }

    // ViewModels
    viewModel { DecagonOnboardingViewModel(get(), get(), get()) }
    viewModel { DecagonWalletViewModel(get(), get(), get()) }

    // Settings ViewModel
    viewModel {
        DecagonSettingsViewModel(
            settingsRepository = get(),
            walletRepository = get(),
            transactionRepository = get(), // ✅ Add
            rpcClient = get() // ✅ Add
        )
    }

    // decagonWalletModule.kt - ADD:
    viewModel {
        DecagonSupportedChainsViewModel(
            walletRepository = get()
        )
    }
}