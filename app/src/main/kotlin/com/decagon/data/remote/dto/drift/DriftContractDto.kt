package com.decagon.data.remote.dto.drift

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Drift API contract DTO.
 * Represents a single perpetual futures or spot market.
 * 
 * NOTE: All numeric fields are returned as STRINGS from the API.
 * Must convert to Double/Long when mapping to Entity.
 * 
 * Example response:
 * ```json
 * {
 *   "contract_index": 0,
 *   "ticker_id": "SOL-PERP",
 *   "base_currency": "SOL",
 *   "quote_currency": "USDC",
 *   "last_price": "238.979183",
 *   "base_volume": "765355.930000000",
 *   "quote_volume": "184422096.257964",
 *   "high": "253.529200",
 *   "low": "235.755600",
 *   "product_type": "PERP",
 *   "open_interest": "1466045.010000000",
 *   "index_price": "238.747713",
 *   "funding_rate": "0.002327164",
 *   "next_funding_rate": "0.002979",
 *   "next_funding_rate_timestamp": "1758301205000"
 * }
 * ```
 */

@Serializable
data class DriftContractDto(
    /**
     * Market index (0, 1, 2, ...)
     */
    @SerialName("contract_index")
    val contractIndex: Int,

    /**
     * Trading pair symbol (e.g., "SOL-PERP", "BTC-PERP")
     */
    @SerialName("ticker_id")
    val tickerId: String,

    /**
     * Base asset (e.g., "SOL", "BTC")
     */
    @SerialName("base_currency")
    val baseCurrency: String,

    /**
     * Quote asset (always "USDC" for Drift)
     */
    @SerialName("quote_currency")
    val quoteCurrency: String,

    /**
     * Last traded price (mark price for perps)
     * STRING - must parse to Double
     */
    @SerialName("last_price")
    val lastPrice: String,

    /**
     * 24h base volume (e.g., "765355.930000000" SOL)
     * STRING - must parse to Double
     */
    @SerialName("base_volume")
    val baseVolume: String,

    /**
     * 24h quote volume in USDC (e.g., "184422096.257964")
     * STRING - must parse to Double
     */
    @SerialName("quote_volume")
    val quoteVolume: String,

    /**
     * 24h high price
     * STRING - must parse to Double
     */
    @SerialName("high")
    val high: String,

    /**
     * 24h low price
     * STRING - must parse to Double
     */
    @SerialName("low")
    val low: String,

    /**
     * Product type: "PERP" or "SPOT"
     * IMPORTANT: Filter by this to get only perpetual futures
     */
    @SerialName("product_type")
    val productType: String,

    /**
     * Open interest (total open positions in base currency)
     * STRING - must parse to Double
     * Example: "1466045.010000000" SOL
     */
    @SerialName("open_interest")
    val openInterest: String,

    /**
     * Index price (spot price of underlying asset)
     * STRING - must parse to Double
     */
    @SerialName("index_price")
    val indexPrice: String,

    /**
     * Current funding rate (8-hour rate as decimal)
     * STRING - must parse to Double
     * Example: "0.002327164" = 0.23% per 8 hours
     */
    @SerialName("funding_rate")
    val fundingRate: String,

    /**
     * Next funding rate (predicted)
     * STRING - must parse to Double
     */
    @SerialName("next_funding_rate")
    val nextFundingRate: String,

    /**
     * Unix timestamp (milliseconds) for next funding
     * STRING - must parse to Long
     */
    @SerialName("next_funding_rate_timestamp")
    val nextFundingRateTimestamp: String
) {
    /**
     * Check if this is a perpetual futures contract.
     * Use this to filter out spot markets.
     */
    val isPerpetual: Boolean get() = productType == "PERP"

    /**
     * Calculate 24h price change percentage.
     * Formula: ((last - low) / low) * 100
     */
    fun calculatePriceChange24h(): Double {
        val last = lastPrice.toDoubleOrNull() ?: return 0.0
        val lowPrice = low.toDoubleOrNull() ?: return 0.0
        val highPrice = high.toDoubleOrNull() ?: return 0.0
        
        if (lowPrice == 0.0) return 0.0
        
        // Use midpoint for better accuracy
        val midpoint = (highPrice + lowPrice) / 2.0
        return ((last - midpoint) / midpoint) * 100.0
    }
}