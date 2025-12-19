package com.decagon.koin.di

import com.decagon.data.repository.DecagonTransactionRepositoryImpl
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.usecase.DecagonSendTokenUseCase
import com.decagon.ui.screen.history.DecagonTransactionDetailViewModel
import com.decagon.ui.screen.history.DecagonTransactionHistoryViewModel
import com.decagon.ui.screen.send.DecagonSendViewModel
import com.koin.data.transaction.TransactionRepositoryImpl
import com.koin.domain.transaction.GetTransactionsUseCase
import com.koin.domain.transaction.TransactionRepository
import com.koin.ui.transactiondetail.TransactionDetailViewModel
import com.koin.ui.transactionhistory.TransactionHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val koinTransactionModule = module {
    // Repository
    single<TransactionRepository> {
        TransactionRepositoryImpl(
           transactionDao  = get(),
            coinDao = get()
        )
    }

    factory {
        GetTransactionsUseCase(
            repository = get(),
        )
    }

    // ViewModels
    viewModel { TransactionDetailViewModel(get(), get()) }

    viewModel {
        TransactionHistoryViewModel(getTransactionsUseCase = get())
    }
}