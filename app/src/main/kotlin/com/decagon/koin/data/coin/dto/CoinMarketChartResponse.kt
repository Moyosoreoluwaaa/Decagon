package com.koin.data.coin.dto

import com.google.gson.annotations.SerializedName

data class CoinMarketChartResponse(
    @SerializedName("prices")
    val prices: List<List<Double>>,
    @SerializedName("market_caps")
    val marketCaps: List<List<Double>>,
    @SerializedName("total_volumes")
    val totalVolumes: List<List<Double>>
)

data class PriceDataPoint(
    val timestamp: Long,
    val price: Double
)

fun CoinMarketChartResponse.toPriceDataPoints(): List<PriceDataPoint> {
    return prices.map { point ->
        PriceDataPoint(
            timestamp = point[0].toLong(),
            price = point[1]
        )
    }
}