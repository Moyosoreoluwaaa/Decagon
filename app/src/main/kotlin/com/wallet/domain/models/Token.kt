package com.octane.wallet.domain.models


/**
 * Domain model for tradable tokens.
 * Used by Discover screen "Tokens" tab.
 */
data class Token(
    val id: String,                    // Unique identifier (e.g., "solana", "bitcoin")
    val symbol: String,                // Ticker symbol (e.g., "SOL", "BTC")
    val name: String,                  // Full name (e.g., "Solana", "Bitcoin")
    val logoUrl: String?,              // Token logo URL
    val currentPrice: Double,          // Current price in USD
    val priceChange24h: Double,        // 24h price change percentage
    val marketCap: Double,             // Market capitalization in USD
    val volume24h: Double,             // 24h trading volume in USD
    val rank: Int,                     // Market cap rank
    val isVerified: Boolean = true,    // Verified/trusted token
    val tags: List<String> = emptyList(), // Categories (e.g., "DeFi", "Gaming")
    val mintAddress: String? = null    // Solana mint address (if applicable)
) {
    /**
     * Formatted price for UI display.
     */
    val formattedPrice: String
        get() = when {
            currentPrice >= 1000 -> "$%.2fK".format(currentPrice / 1000)
            currentPrice >= 1 -> "$%.2f".format(currentPrice)
            currentPrice >= 0.01 -> "$%.4f".format(currentPrice)
            else -> "$%.6f".format(currentPrice)
        }

    /**
     * Formatted market cap for UI display.
     */
    val formattedMarketCap: String
        get() = when {
            marketCap >= 1_000_000_000 -> "$%.2fB".format(marketCap / 1_000_000_000)
            marketCap >= 1_000_000 -> "$%.2fM".format(marketCap / 1_000_000)
            else -> "$%.2fK".format(marketCap / 1_000)
        }

    /**
     * Price change indicator for UI.
     */
    val priceChangeFormatted: String
        get() {
            val sign = if (priceChange24h > 0) "+" else ""
            return "$sign%.2f%%".format(priceChange24h)
        }

    val isPriceUp: Boolean get() = priceChange24h > 0
}
