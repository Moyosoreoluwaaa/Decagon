package com.decagon.di

import com.decagon.data.remote.JupiterUltraApiService
import com.decagon.data.repository.SwapRepositoryImpl
import com.decagon.domain.repository.SwapRepository
import com.decagon.domain.usecase.*
import com.decagon.ui.screen.swap.SwapViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val decagonSwapModule = module {

    // API Service
    single {
        JupiterUltraApiService(
            httpClient = get(),
            baseUrl = "https://lite-api.jup.ag", // TODO: Migrate to api.jup.ag after Dec 31, 2025
            apiKey = "73ca3287-e794-4b2b-8f1d-02bc8bf60e8f"
        )
    }

    // Repository
    single<SwapRepository> {
        SwapRepositoryImpl(
            apiService = get(),
            swapHistoryDao = get(),
            tokenCacheDao = get()
        )
    }

    // Use Cases
    factory { GetSwapQuoteUseCase(get()) }

    // Execute Swap - now with RpcClientFactory
    factory {
        ExecuteSwapUseCase(
            swapRepository = get(),
            walletRepository = get(),
            keyDerivation = get(),
            biometricAuthenticator = get(),
            rpcFactory = get()  // ← CHANGED: Factory instead of client
        )
    }

    factory { SearchTokensUseCase(get()) }
    factory { GetTokenBalancesUseCase(get()) }
    factory { ValidateTokenSecurityUseCase(get()) }
    factory { GetSwapHistoryUseCase(get()) }

    // ViewModel
    viewModel {
        SwapViewModel(
            getSwapQuoteUseCase = get(),
            executeSwapUseCase = get(),
            searchTokensUseCase = get(),
            getTokenBalancesUseCase = get(),
            validateSecurityUseCase = get(),
            getSwapHistoryUseCase = get(),
            walletRepository = get()
        )
    }
}