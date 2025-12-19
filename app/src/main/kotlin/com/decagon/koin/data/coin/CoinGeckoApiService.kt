package com.koin.data.coin

import com.koin.data.coin.dto.CoinMarketChartResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinGeckoApiService {
    @GET("coins/markets")
    suspend fun getCoinsWithFullDetails(
        @Query("vs_currency") currency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 100,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true,
        @Query("price_change_percentage") priceChange: String = "1h,24h,7d,30d"
    ): List<CoinDetailDto>
    
    @GET("simple/price")
    suspend fun getSimplePrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd",
        @Query("include_market_cap") includeMarketCap: Boolean = false,
        @Query("include_24hr_vol") include24hrVol: Boolean = false,
        @Query("include_24hr_change") include24hrChange: Boolean = true,
        @Query("include_last_updated_at") includeLastUpdatedAt: Boolean = true
    ): Map<String, Map<String, Any>>
    
    @GET("coins/{id}/market_chart/range")
    suspend fun getCoinMarketChartRange(
        @Path("id") id: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("from") from: Long,
        @Query("to") to: Long
    ): CoinMarketChartResponse
}