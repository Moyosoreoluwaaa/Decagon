package com.koin.domain.model

import java.text.NumberFormat
import java.util.Locale

data class Coin(
    val id: String,
    val name: String,
    val symbol: String,
    val imageUrl: String,
    val currentPrice: Double,
    val marketCap: Long,
    val marketCapRank: Int,
    val priceChange24h: Double,
    val priceChangePercentage24h: Double,
    val priceChangePercentage1h: Double?,
    val priceChangePercentage7d: Double?,
    val priceChangePercentage30d: Double?,
    val sparklineData: List<Double>?,
    val high24h: Double?,
    val low24h: Double?,
    val totalVolume: Double?,
    val circulatingSupply: Double?,
    val totalSupply: Double?,
    val maxSupply: Double?,
    val ath: Double?,
    val athDate: String?,
    val atl: Double?,
    val atlDate: String?
) {
    val isPositive24h: Boolean get() = priceChangePercentage24h > 0
    val formattedPrice: String get() = NumberFormat.getCurrencyInstance().format(currentPrice)
    val formattedMarketCap: String get() = formatLargeNumber(marketCap)
    val formattedPriceChange: String
        get() = "${if (isPositive24h) "+" else ""}${
            String.format(
                Locale.US, "%.2f", priceChangePercentage24h
            )
        }%"

    val supplyPercentage: Double?
        get() =
            if (totalSupply != null && circulatingSupply != null)
                (circulatingSupply / totalSupply) * 100
            else null
    val formattedVolume: String
        get() = totalVolume?.toLong()?.let { formatLargeNumber(it) } ?: "N/A"
    val formattedSupply: String
        get() = circulatingSupply?.let { formatLargeNumber(it.toLong()) } ?: "N/A"
}

private fun formatLargeNumber(number: Long): String {
    return when {
        number >= 1_000_000_000_000 -> "${String.format(Locale.US,"%.1f", number / 1_000_000_000_000.0)}T"
        number >= 1_000_000_000 -> "${String.format(Locale.US,"%.1f", number / 1_000_000_000.0)}B"
        number >= 1_000_000 -> "${String.format(Locale.US,"%.1f", number / 1_000_000.0)}M"
        number >= 1_000 -> "${String.format(Locale.US,"%.1f", number / 1_000.0)}K"
        else -> number.toString()
    }
}