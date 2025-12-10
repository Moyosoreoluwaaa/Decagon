package com.decagon.di

import com.decagon.data.remote.CoinPriceService
import com.decagon.data.remote.SolanaRpcClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val decagonNetworkModule = module {
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }

            // ✅ ADD: Timeout plugin
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }
    }

    single {
        SolanaRpcClient(
            httpClient = get(),
            rpcUrl = "https://api.devnet.solana.com"
        )
    }

    single { CoinPriceService(get()) }
}