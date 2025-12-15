package com.decagon.di

import com.decagon.data.repository.DecagonTransactionRepositoryImpl
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.usecase.DecagonSendTokenUseCase
import com.decagon.ui.screen.history.DecagonTransactionDetailViewModel
import com.decagon.ui.screen.history.DecagonTransactionHistoryViewModel
import com.decagon.ui.screen.send.DecagonSendViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val decagonTransactionModule = module {
    // Repository
    single<DecagonTransactionRepository> {
        DecagonTransactionRepositoryImpl(
            pendingTxDao = get(),
            transactionDao = get()
        )
    }

    // Use Case - now with RpcClientFactory
    factory {
        DecagonSendTokenUseCase(
            walletRepository = get(),
            transactionRepository = get(),
            rpcFactory = get(),  // ← CHANGED: Factory instead of client
            keyDerivation = get(),
            biometricAuthenticator = get()
        )
    }

    // ViewModels
    viewModel { DecagonSendViewModel(get()) }

    viewModel {
        DecagonTransactionHistoryViewModel(
            transactionRepository = get(),
            walletRepository = get()
        )
    }

    viewModel {
        DecagonTransactionDetailViewModel(
            transactionRepository = get()
        )
    }
}