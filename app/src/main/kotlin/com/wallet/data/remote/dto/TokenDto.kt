package com.wallet.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * CoinGecko API response for token data.
 * Endpoint: /coins/markets
 */
@Serializable
data class TokenDto(
    @SerialName("id")
    val id: String,

    @SerialName("symbol")
    val symbol: String,

    @SerialName("name")
    val name: String,

    @SerialName("image")
    val image: String?,

    @SerialName("current_price")
    val currentPrice: Double,

    @SerialName("price_change_percentage_24h")
    val priceChange24h: Double?,

    @SerialName("market_cap")
    val marketCap: Double,

    @SerialName("total_volume")
    val totalVolume: Double,

    @SerialName("market_cap_rank")
    val marketCapRank: Int?,

    // Solana-specific (if available)
    @SerialName("platforms")
    val platforms: Map<String, String>? = null
)
