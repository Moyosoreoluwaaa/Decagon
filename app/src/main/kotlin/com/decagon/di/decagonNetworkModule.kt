package com.decagon.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import com.decagon.core.network.NetworkManager
import com.decagon.core.network.NetworkManagerImpl
import com.decagon.core.network.RpcClientFactory
import com.decagon.data.remote.CoinPriceService
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module

private val Context.networkDataStore by preferencesDataStore(name = "network_settings")

val decagonNetworkModule = module {
    // HTTP Client with timeout configuration
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
        }
    }

    // Network Manager for network switching
    single<NetworkManager> {
        NetworkManagerImpl(
            dataStore = get<Context>().networkDataStore
        )
    }

    // RPC Client Factory - creates network-aware clients dynamically
    single {
        RpcClientFactory(
            httpClient = get(),
            networkManager = get()
        )
    }

    // Coin Price Service
    single { CoinPriceService(get()) }
}