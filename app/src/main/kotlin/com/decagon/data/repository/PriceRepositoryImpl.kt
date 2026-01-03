package com.decagon.data.repository

import com.decagon.data.remote.api.PriceApi
import com.decagon.data.remote.dto.price.PriceResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Real implementation for fetching crypto prices.
 * Uses CoinGecko API for price data.
 */
class PriceRepositoryImpl(
    private val priceApi: PriceApi
) : PriceRepository {

    /**
     * Token mint to CoinGecko ID mapping for Solana tokens.
     */
    private val tokenMapping = mapOf(
        // Native SOL
        "sol" to "solana",
        
        // Stablecoins
        "usdc" to "usd-coin",
        "usdt" to "tether",
        
        // Popular Solana tokens
        "ray" to "raydium",
        "srm" to "serum",
        "orca" to "orca",
        "mngo" to "mango-markets",
        "bonk" to "bonk",
        "jup" to "jupiter-exchange-solana",
        "wif" to "dogwifcoin",
        "pyth" to "pyth-network",
        
        // Wrapped tokens
        "wbtc" to "wrapped-bitcoin",
        "weth" to "weth",
        "wbnb" to "wbnb",
        
        // By mint address (common ones)
        "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v" to "usd-coin", // USDC
        "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB" to "tether", // USDT
        "So11111111111111111111111111111111111111112" to "solana", // Wrapped SOL
        "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263" to "bonk", // BONK
        "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN" to "jupiter-exchange-solana" // JUP
    )

    override suspend fun fetchPrices(
        symbols: List<String>,
        currency: String
    ): Result<Map<String, PriceResponse>> {
        return try {
            // Map symbols/mints to CoinGecko IDs
            val coinIds = symbols.mapNotNull { symbol ->
                tokenMapping[symbol.lowercase()] ?: tokenMapping[symbol]
            }.distinct().joinToString(",")

            if (coinIds.isEmpty()) {
                return Result.success(emptyMap())
            }

            // Fetch from CoinGecko
            val response = priceApi.getPrices(
                coinIds = coinIds,
                currency = currency,
                include24hrChange = true
            )

            // Reverse map back to original symbols
            val mappedResponse = mutableMapOf<String, PriceResponse>()
            response.forEach { (coinGeckoId, priceData) ->
                // Find original symbol(s) for this coinGeckoId
                tokenMapping.forEach { (originalSymbol, mappedId) ->
                    if (mappedId == coinGeckoId && originalSymbol in symbols.map { it.lowercase() }) {
                        mappedResponse[originalSymbol] = priceData
                    }
                }
            }

            Result.success(mappedResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchPrice(
        symbol: String,
        currency: String
    ): Result<PriceResponse> {
        return try {
            val coinId = tokenMapping[symbol.lowercase()] ?: symbol.lowercase()
            
            val response = priceApi.getPrices(
                coinIds = coinId,
                currency = currency,
                include24hrChange = true
            )

            val priceData = response[coinId]
            if (priceData != null) {
                Result.success(priceData)
            } else {
                Result.failure(Exception("Price not found for $symbol"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observePrices(
        symbols: List<String>,
        currency: String,
        intervalMs: Long
    ): Flow<Map<String, PriceResponse>> = flow {
        while (true) {
            val result = fetchPrices(symbols, currency)
            result.getOrNull()?.let { emit(it) }
            delay(intervalMs)
        }
    }

    /**
     * Add custom token mapping for unknown tokens.
     */
    fun addTokenMapping(symbolOrMint: String, coinGeckoId: String) {
        (tokenMapping as MutableMap)[symbolOrMint.lowercase()] = coinGeckoId
    }
}

/**
 * Repository interface for price data.
 */
interface PriceRepository {
    suspend fun fetchPrices(
        symbols: List<String>,
        currency: String = "usd"
    ): Result<Map<String, PriceResponse>>

    suspend fun fetchPrice(
        symbol: String,
        currency: String = "usd"
    ): Result<PriceResponse>

    fun observePrices(
        symbols: List<String>,
        currency: String = "usd",
        intervalMs: Long = 30_000L
    ): Flow<Map<String, PriceResponse>>
}