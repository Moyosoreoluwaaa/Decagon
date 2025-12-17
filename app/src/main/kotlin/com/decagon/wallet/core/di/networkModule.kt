package com.octane.wallet.core.di
import com.octane.wallet.core.blockchain.JupiterApiService
import com.octane.wallet.core.network.JupiterApiServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import timber.log.Timber
/**
 * Network Module - provides HTTP clients with proper TLS configuration
 */
val networkModule = module {
// ===== SHARED JSON CONFIGURATION =====
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true // ✅ Handle null -> default values
        }
    }
// ===== SOLANA RPC HTTP CLIENT =====
    single(named("SolanaRpcHttpClient")) {
        HttpClient(CIO) {
            engine {
// ✅ TLS configuration
                https {
// Trust system certificates
                    trustManager = null // Use platform default
                }
// Connection pooling
                maxConnectionsCount = 100
                endpoint {
                    connectTimeout = 30_000
                    socketTimeout = 30_000
                    connectAttempts = 3
                }
            }
            install(ContentNegotiation) {
                json(get()) // Use shared Json configuration
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("SolanaRPC").d(message)
                    }
                }
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            expectSuccess = true // Throw on 4xx/5xx
        }
    }
// ===== COINGECKO HTTP CLIENT =====
    single(named("CoinGeckoHttpClient")) {
        HttpClient(CIO) {
            engine {
// ✅ TLS configuration for CoinGecko
                https {
                    trustManager = null // Use platform default
                }
                maxConnectionsCount = 50
                endpoint {
                    connectTimeout = 15_000
                    socketTimeout = 15_000
                    connectAttempts = 2
                }
            }
            install(ContentNegotiation) {
                json(get())
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("CoinGecko").d(message)
                    }
                }
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
            expectSuccess = true
        }
    }
// ===== JUPITER HTTP CLIENT =====
    single(named("JupiterHttpClient")) {
        HttpClient(CIO) {
            engine {
// ✅ TLS configuration for Jupiter/Drift/DeFiLlama
                https {
                    trustManager = null // Use platform default
                }
                maxConnectionsCount = 50
                endpoint {
                    connectTimeout = 30_000
                    socketTimeout = 30_000
                    connectAttempts = 3
                }
            }
            install(ContentNegotiation) {
                json(get())
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.tag("JupiterAPI").d(message)
                    }
                }
                level = LogLevel.INFO
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 30_000
            }
            expectSuccess = true
        }
    }
// ===== LEGACY JUPITER API SERVICE =====
    single<JupiterApiService> {
        JupiterApiServiceImpl(httpClient = get(named("JupiterHttpClient")))
    }
}