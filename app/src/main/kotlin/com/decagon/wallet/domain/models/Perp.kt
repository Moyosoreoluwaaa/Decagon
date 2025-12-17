package com.octane.wallet.domain.models

/**
 * Domain model for perpetual futures (Perps).
 * Used by Discover screen "Perps" tab.
 */
data class Perp(
    val id: String,                    // Unique identifier
    val symbol: String,                // Trading pair (e.g., "SOL-PERP", "BTC-PERP")
    val name: String,                  // Full name
    val logoUrl: String?,              // Logo URL
    val indexPrice: Double,            // Spot price (underlying asset)
    val markPrice: Double,             // Futures price
    val fundingRate: Double,           // Current funding rate (%)
    val nextFundingTime: Long,         // Unix timestamp for next funding
    val openInterest: Double,          // Total open positions in USD
    val volume24h: Double,             // 24h volume in USD
    val priceChange24h: Double,        // 24h price change percentage
    val leverage: String = "20x",      // Max leverage available
    val exchange: String = "Jupiter",  // Exchange/protocol
    val isLong: Boolean = true         // Default position direction hint
) {
    /**
     * Formatted funding rate for UI.
     */
    val formattedFundingRate: String
        get() {
            val sign = if (fundingRate > 0) "+" else ""
            return "$sign%.4f%%".format(fundingRate)
        }

    /**
     * Formatted open interest for UI.
     */
    val formattedOpenInterest: String
        get() = when {
            openInterest >= 1_000_000_000 -> "$%.2fB".format(openInterest / 1_000_000_000)
            openInterest >= 1_000_000 -> "$%.2fM".format(openInterest / 1_000_000)
            else -> "$%.2fK".format(openInterest / 1_000)
        }

    /**
     * Funding rate color indicator.
     */
    val isFundingPositive: Boolean get() = fundingRate > 0
}
