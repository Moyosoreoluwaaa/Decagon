package com.decagon.di

import com.decagon.data.provider.MoonPayProvider
import com.decagon.data.provider.RampProvider
import com.decagon.data.provider.TransakProvider
import com.decagon.data.remote.api.CoinPriceService
import com.decagon.data.remote.api.DeFiLlamaApi
import com.decagon.data.remote.api.DiscoverApi
import com.decagon.data.remote.api.DriftApi
import com.decagon.data.remote.api.JupiterUltraApiService
import com.decagon.data.remote.api.PriceApi
import com.decagon.data.remote.api.SolanaRpcApi
import com.decagon.data.remote.api.SwapAggregatorApi
import com.decagon.data.remote.api.createDeFiLlamaApi
import com.decagon.data.remote.api.createDiscoverApi
import com.decagon.data.remote.api.createDriftApi
import com.decagon.data.remote.api.createPriceApi
import com.decagon.data.remote.api.createSolanaRpcApi
import com.decagon.data.remote.api.createSwapAggregatorApi
import com.decagon.data.repository.DecagonOnboardingStateRepository
import com.decagon.data.repository.DecagonOnboardingStateRepositoryImpl
import com.decagon.data.repository.DecagonSettingsRepositoryImpl
import com.decagon.data.repository.DecagonTransactionRepositoryImpl
import com.decagon.data.repository.DecagonWalletRepositoryImpl
import com.decagon.data.repository.DiscoverRepositoryImpl
import com.decagon.data.repository.OnRampRepositoryImpl
import com.decagon.data.repository.SwapRepositoryImpl
import com.decagon.data.service.JupiterSwapService
import com.decagon.data.service.TokenLogoResolver
import com.decagon.domain.repository.DecagonSettingsRepository
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.repository.DiscoverRepository
import com.decagon.domain.repository.OnRampRepository
import com.decagon.domain.repository.SwapRepository
import com.decagon.core.network.ApiConfig
import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dataModule = module {
    // ===== HTTP CLIENTS (with TLS/Pooling) =====
    single(named("SolanaRpcHttpClient")) {
        HttpClient(CIO) {
            install(ContentNegotiation) { json(get()) }
            install(HttpTimeout) { requestTimeoutMillis = 30_000 }
        }
    }
    single(named("CoinGeckoHttpClient")) {
        HttpClient(CIO) {
            install(ContentNegotiation) { json(get()) }
            install(HttpTimeout) { requestTimeoutMillis = 15_000 }
        }
    }
    single(named("JupiterHttpClient")) {
        HttpClient(CIO) {
            install(ContentNegotiation) { json(get()) }
            install(HttpTimeout) { requestTimeoutMillis = 30_000 }
        }
    }

    // ===== KTORFIT APIs =====

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) { json(get()) }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 30_000
            }
        }
    }
    single<SolanaRpcApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.Solana.MAINNET_PUBLIC)
            .httpClient(get<HttpClient>(named("SolanaRpcHttpClient"))) // Type specified here
            .build()
            .createSolanaRpcApi()
    }

    single<PriceApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.COINGECKO_BASE_URL)
            .httpClient(get<HttpClient>(named("CoinGeckoHttpClient"))) // Type specified here
            .build()
            .createPriceApi()
    }

    single<SwapAggregatorApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.JUPITER_BASE_URL)
            .httpClient(get<HttpClient>(named("JupiterHttpClient"))) // Type specified here
            .build()
            .createSwapAggregatorApi()
    }

    single<DiscoverApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.COINGECKO_BASE_URL)
            .httpClient(get<HttpClient>(named("CoinGeckoHttpClient"))) // Type specified here
            .build()
            .createDiscoverApi()
    }

    single<DeFiLlamaApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.DEFILLAMA_BASE_URL)
            .httpClient(get<HttpClient>(named("JupiterHttpClient"))) // Type specified here
            .build()
            .createDeFiLlamaApi()
    }

    single<DriftApi> {
        Ktorfit.Builder()
            .baseUrl(ApiConfig.DRIFT_URL)
            .httpClient(get<HttpClient>(named("JupiterHttpClient"))) // Type specified here
            .build()
            .createDriftApi()
    }

    // ===== SERVICES =====
    single { JupiterSwapService(get()) }
    single { TokenLogoResolver(get()) }
    single { CoinPriceService(get()) }
    single {
        JupiterUltraApiService(
            get(),
            "https://lite-api.jup.ag",
            "73ca3287-e794-4b2b-8f1d-02bc8bf60e8f"
        )
    }

    // ===== REPOSITORIES =====
    single<DecagonWalletRepository> {
        DecagonWalletRepositoryImpl(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<DecagonTransactionRepository> { DecagonTransactionRepositoryImpl(get(), get()) }
    single<DiscoverRepository> { DiscoverRepositoryImpl(get(), get(), get(), get(), get()) }
    single<SwapRepository> { SwapRepositoryImpl(get(), get(), get()) }
    single<OnRampRepository> { OnRampRepositoryImpl(get()) }
    single<DecagonSettingsRepository> { DecagonSettingsRepositoryImpl(get(), get(), get(), get()) }
    single<DecagonOnboardingStateRepository> { DecagonOnboardingStateRepositoryImpl(get()) }

    // ===== PROVIDERS =====
    single { RampProvider() }
    single { MoonPayProvider() }
    single { TransakProvider() }
}