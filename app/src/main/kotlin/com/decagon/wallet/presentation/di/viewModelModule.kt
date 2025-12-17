package com.octane.wallet.presentation.di

import com.decagon.wallet.presentation.viewmodel.PerpDetailViewModel
import com.octane.wallet.presentation.viewmodel.*
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    // ==================== Base ViewModels (Shared State) ====================
    single { BaseWalletViewModel(get(), get(), get(), get(), get(), get()) }
    single { BasePortfolioViewModel(get(), get(), get(), get(), get()) }
    single { BaseTransactionViewModel(get(), get(), get()) }

    // ==================== Feature ViewModels ====================

    // Home
    viewModel { HomeViewModel(get(), get(), get()) }

    // Wallet/Activity
    viewModel { ActivityViewModel(get(), get()) }

    // Wallets Management
    viewModel { WalletsViewModel(get()) }

    // Send
    viewModel { SendViewModel(get(), get(), get(), get(), get(), get()) }

    // Receive
    viewModel { ReceiveViewModel(get(), get(), get()) }

    // Swap
    viewModel { SwapViewModel(get(), get(), get(), get(), get(), get()) }

    // Settings
    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get()) }

    // Token Details
    viewModel { TokenDetailViewModel(get()) }

    // Manage Tokens
    viewModel { ManageTokensViewModel(get(), get()) }

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

    // Staking (V1.1)
    viewModel { StakingViewModel(get(), get(), get(), get()) }

    // Security (V1.8)
    viewModel { SecurityViewModel(get(), get()) }

    viewModel { TransactionDetailsViewModel(get(), get()) }
}