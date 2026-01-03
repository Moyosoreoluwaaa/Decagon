package com.decagon.data.remote.api

import com.decagon.data.remote.dto.TokenDto
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.serialization.Serializable

/**
 * Discover API - CoinGecko endpoints for tokens only.
 * ✅ REMOVED: dApp endpoints (moved to DeFiLlamaApi)
 */
interface DiscoverApi {

    // ==================== TOKENS ====================

    @GET("coins/markets")
    suspend fun getTokens(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = false,
        @Query("price_change_percentage") priceChangePercentage: String = "24h"
    ): List<TokenDto>

    @GET("search")
    suspend fun searchTokens(
        @Query("query") query: String
    ): TokenSearchResponse
}

@Serializable
data class TokenSearchResponse(
    val coins: List<TokenSearchResult>
)

@Serializable
data class TokenSearchResult(
    val id: String,
    val name: String,
    val symbol: String,
    val thumb: String?,
    val market_cap_rank: Int?
)