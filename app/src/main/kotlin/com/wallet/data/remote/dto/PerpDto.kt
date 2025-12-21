package com.octane.wallet.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Jupiter Perps API response.
 * Endpoint: Custom aggregator or Drift Protocol API
 */
@Serializable
data class PerpDto(
    @SerialName("symbol")
    val symbol: String,

    @SerialName("base_asset")
    val baseAsset: String,

    @SerialName("quote_asset")
    val quoteAsset: String,

    @SerialName("index_price")
    val indexPrice: Double,

    @SerialName("mark_price")
    val markPrice: Double,

    @SerialName("funding_rate")
    val fundingRate: Double,

    @SerialName("next_funding_time")
    val nextFundingTime: Long,

    @SerialName("open_interest")
    val openInterest: Double,

    @SerialName("volume_24h")
    val volume24h: Double,

    @SerialName("price_change_24h")
    val priceChange24h: Double,

    @SerialName("max_leverage")
    val maxLeverage: String? = "20x",

    @SerialName("exchange")
    val exchange: String? = "Jupiter"
)
