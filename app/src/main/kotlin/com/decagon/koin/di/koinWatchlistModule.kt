package com.decagon.koin.di

import com.koin.data.transaction.TransactionRepositoryImpl
import com.koin.data.watchlist.WatchlistRepositoryImpl
import com.koin.domain.transaction.GetTransactionsUseCase
import com.koin.domain.watchlist.WatchlistRepository
import com.koin.ui.coindetail.CoinDetailViewModel
import com.koin.ui.coinlist.CoinListViewModel
import com.koin.ui.profile.ProfileViewModel
import com.koin.ui.transactiondetail.TransactionDetailViewModel
import com.koin.ui.transactionhistory.TransactionHistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val koinWatchlistModule = module {
    // Repository
    single<WatchlistRepository> {
        WatchlistRepositoryImpl(watchlistDao  = get())
    }

    factory {
        GetTransactionsUseCase(
            repository = get(),
        )
    }

    // ViewModels
    viewModel { CoinDetailViewModel(get(), get(), get(), get(), get()) }

    viewModel {
        CoinListViewModel(get(),get(),get(), get(),)
    }
}