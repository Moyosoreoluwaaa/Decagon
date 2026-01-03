package com.decagon.data.remote.api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber

class CoinPriceService(
    private val httpClient: HttpClient
) {
    // CoinGecko Free API - supports 150+ fiat currencies
    private val baseUrl = "https://api.coingecko.com/api/v3"
    
    suspend fun getPrices(
        coinIds: List<String>,
        vsCurrency: String = "usd"
    ): Result<Map<String, Double>> {
        return try {
            val ids = coinIds.joinToString(",")
            val response: Map<String, CoinPrice> = httpClient.get(
                "$baseUrl/simple/price?ids=$ids&vs_currencies=$vsCurrency"
            ).body()
            
            val prices = response.mapValues { (_, price) ->
                price.prices[vsCurrency] ?: 0.0
            }
            
            Timber.d("Fetched prices: $prices")
            Result.success(prices)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch prices")
            Result.failure(e)
        }
    }
    
    @Serializable
    private data class CoinPrice(
        @SerialName("usd") val usd: Double? = null,
        @SerialName("ngn") val ngn: Double? = null,
        @SerialName("eur") val eur: Double? = null,
        @SerialName("gbp") val gbp: Double? = null
    ) {
        val prices get() = mapOf(
            "usd" to (usd ?: 0.0),
            "ngn" to (ngn ?: 0.0),
            "eur" to (eur ?: 0.0),
            "gbp" to (gbp ?: 0.0)
        )
    }
    
    companion object {
        const val COIN_ID_SOLANA = "solana"
        const val COIN_ID_ETHEREUM = "ethereum"
        const val COIN_ID_POLYGON = "matic-network"
    }
}