package com.octane.wallet.data.di

import com.wallet.data.repository.AssetRepositoryImpl
import com.wallet.data.repository.DiscoverRepositoryImpl
import com.wallet.data.repository.TransactionRepositoryImpl
import com.wallet.data.repository.WalletRepositoryImpl
import com.wallet.domain.repository.AssetRepository
import com.wallet.domain.repository.DiscoverRepository
import com.wallet.domain.repository.TransactionRepository
import com.wallet.domain.repository.WalletRepository
import org.koin.dsl.module

/**
 * Koin module for Repository implementations.
 */
val repositoryModule = module {

    single<WalletRepository> {
        WalletRepositoryImpl(
            walletDao = get()
        )
    }

    single<AssetRepository> {
        AssetRepositoryImpl(
            assetDao = get(),
            solanaRpcApi = get(),
            priceApi = get(),
            networkMonitor = get()
        )
    }

    single<TransactionRepository> {
        TransactionRepositoryImpl(
            transactionDao = get(),
            solanaRpcApi = get(),
            networkMonitor = get()
        )
    }

    single<WalletRepository> { WalletRepositoryImpl(get()) }
    single<AssetRepository> { AssetRepositoryImpl(get(), get(), get(), get()) }
    single<TransactionRepository> { TransactionRepositoryImpl(get(), get(), get()) }

    single<DiscoverRepository> {
        DiscoverRepositoryImpl(
            discoverApi = get(),
            defiLlamaApi = get(),
            discoverDao = get(),
            networkMonitor = get(),
            driftApi = get(),
            tokenLogoResolver = get()
        )
    }
}