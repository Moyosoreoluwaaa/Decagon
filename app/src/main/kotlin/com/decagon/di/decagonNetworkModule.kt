package com.decagon.di

import com.decagon.data.remote.CoinPriceService
import com.decagon.data.remote.SolanaRpcClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
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