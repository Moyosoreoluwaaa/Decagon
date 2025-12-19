package com.decagon.koin.di

import com.decagon.data.repository.DecagonTransactionRepositoryImpl
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.usecase.DecagonSendTokenUseCase
import com.decagon.ui.screen.history.DecagonTransactionDetailViewModel
import com.decagon.ui.screen.history.DecagonTransactionHistoryViewModel
import com.decagon.ui.screen.send.DecagonSendViewModel
import com.koin.data.portfolio.PortfolioRepositoryImpl
import com.koin.data.transaction.TransactionRepositoryImpl
import com.koin.domain.portfolio.BuyCoinUseCase
import com.koin.domain.portfolio.GetBalanceUseCase
import com.koin.domain.portfolio.GetPortfolioUseCase
import com.koin.domain.portfolio.GetTransactionHistoryUseCase
import com.koin.domain.portfolio.PortfolioRepository
import com.koin.domain.portfolio.RefreshPortfolioUseCase
import com.koin.domain.portfolio.ResetPortfolioUseCase
import com.koin.domain.portfolio.SellCoinUseCase
import com.koin.domain.transaction.GetTransactionsUseCase
import com.koin.domain.transaction.TransactionRepository
import com.koin.ui.portfolio.PortfolioViewModel
import com.koin.ui.transactiondetail.TransactionDetailViewModel
import com.koin.ui.transactionhistory.TransactionHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val koinPortfolioModule = module {
    // Repository
    single<PortfolioRepository> {
        PortfolioRepositoryImpl(
            portfolioDao = get(),
            notificationService = get()
        )
    }

    factory { GetPortfolioUseCase(repository = get()) }
    factory { BuyCoinUseCase(repository = get()) }
    factory { SellCoinUseCase(repository = get()) }
    factory { RefreshPortfolioUseCase(repository = get()) }
    factory { GetTransactionHistoryUseCase(repository = get()) }
    factory { GetBalanceUseCase(repository = get()) }
    factory { ResetPortfolioUseCase(repository = get()) }

    // ViewModels
    viewModel { PortfolioViewModel(get(), get(), get(), get(), get(), get(), get(), get()) }
}