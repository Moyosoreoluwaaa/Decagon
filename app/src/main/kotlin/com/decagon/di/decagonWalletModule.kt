package com.decagon.di

import com.decagon.data.repository.DecagonWalletRepositoryImpl
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.usecase.DecagonCreateWalletUseCase
import com.decagon.ui.screen.onboarding.DecagonOnboardingViewModel
import com.decagon.ui.screen.wallet.DecagonWalletViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
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
    
    // UseCases
    factory { DecagonCreateWalletUseCase(get(), get()) }
    
    // ViewModels
    viewModel { DecagonOnboardingViewModel(get(), get()) }
    viewModel { DecagonWalletViewModel(get(), get()) }
}