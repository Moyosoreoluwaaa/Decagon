package com.decagon.data.remote.dto.price

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PriceResponse(
    @SerialName("usd")
    val usd: Double? = null,

    @SerialName("usd_24h_change")
    val usd24hChange: Double? = null,

    @SerialName("usd_market_cap")
    val usdMarketCap: Double? = null,

    @SerialName("usd_24h_vol")
    val usd24hVol: Double? = null
)
