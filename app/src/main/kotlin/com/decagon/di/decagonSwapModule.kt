package com.decagon.di

import com.decagon.data.remote.DecagonJupiterSwapService
import com.decagon.data.repository.DecagonSwapRepositoryImpl
import com.decagon.domain.repository.DecagonSwapRepository
import org.koin.dsl.module

val decagonSwapModule = module {
    
    /**
     * Jupiter Swap Service (Singleton)
     * 
     * HTTP client for Jupiter Aggregator API.
     * Handles quote fetching and transaction building.
     */
    single {
        DecagonJupiterSwapService(
            httpClient = get(), // From decagonNetworkModule
            baseUrl = if (com.decagon.BuildConfig.DEBUG) {
                // Devnet URL for development (quotes work, but no real liquidity)
                "https://quote-api.jup.ag/v6"
            } else {
                // Mainnet URL for production
                "https://quote-api.jup.ag/v6"
            }
        )
    }
    
    /**
     * Swap Repository (Singleton)
     * 
     * Coordinates between Jupiter API and local database.
     * Implements offline-first pattern with caching.
     */
    single<DecagonSwapRepository> {
        DecagonSwapRepositoryImpl(
            jupiterService = get(),
            swapHistoryDao = get(),
            cachedTokenDao = get()
        )
    }
}