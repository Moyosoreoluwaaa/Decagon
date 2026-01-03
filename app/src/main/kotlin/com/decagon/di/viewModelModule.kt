package com.decagon.di

import com.decagon.ui.screen.all.*
import com.decagon.ui.screen.chains.DecagonSupportedChainsViewModel
import com.decagon.ui.screen.discover.DiscoverViewModel
import com.decagon.ui.screen.history.*
import com.decagon.ui.screen.onboarding.DecagonOnboardingViewModel
import com.decagon.ui.screen.onramp.DecagonOnRampViewModel
import com.decagon.ui.screen.perps.PerpDetailViewModel
import com.decagon.ui.screen.send.DecagonSendViewModel
import com.decagon.ui.screen.settings.*
import com.decagon.ui.screen.swap.SwapViewModel
import com.decagon.ui.screen.token.TokenDetailViewModel
import com.decagon.ui.screen.wallet.DecagonWalletViewModel
import com.decagon.ui.viewmodels.*
import com.decagon.worker.TransactionCleanupWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    // Main Feature ViewModels
    viewModel { DecagonWalletViewModel(get(), get(), get(), get()) }
    viewModel { SwapViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { DecagonSendViewModel(get()) }
    viewModel { DecagonOnRampViewModel(get(), get(), get()) }

    // Discover & Details
    viewModel {
        DiscoverViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get())
    }
    viewModel { TokenDetailViewModel(get()) }
    viewModel { PerpDetailViewModel(get()) }
    viewModel { AllTokensViewModel(get(), get(), get()) }
    viewModel { AllPerpsViewModel(get(), get()) }
    viewModel { AllDAppsViewModel(get(), get()) }

    // Settings & History
    viewModel { DecagonSettingsViewModel(get(), get(), get(), get()) }
    viewModel { DecagonTransactionHistoryViewModel(get(), get()) }
    viewModel { DecagonTransactionDetailViewModel(get()) }
    viewModel { DecagonSupportedChainsViewModel(get()) }

    // Infrastructure ViewModels
    viewModel { DAppBrowserViewModel(get(), get(), get(), get()) }
    viewModel { DecagonOnboardingViewModel(get(), get(), get()) }
    viewModel { SessionViewModel(get()) }

    // Workers
    worker {
        TransactionCleanupWorker(
            context = get(),
            params = get(),
            transactionRepository = get(),
            walletRepository = get(),
            rpcFactory = get()
        )
    }
}