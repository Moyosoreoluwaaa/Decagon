package com.decagon.di

import com.decagon.data.repository.DecagonTransactionRepositoryImpl
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.usecase.DecagonSendTokenUseCase
import com.decagon.ui.screen.send.DecagonSendViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val decagonTransactionModule = module {
    single<DecagonTransactionRepository> {
        DecagonTransactionRepositoryImpl(
            pendingTxDao = get(),
            transactionDao = get()
        )
    }
    factory {
        DecagonSendTokenUseCase(
            walletRepository = get(),
            transactionRepository = get(),
            rpcClient = get(),
            keyDerivation = get(),
            biometricAuthenticator = get()
        )
    }

    viewModel { DecagonSendViewModel(get()) }
}