package com.decagon.ui.utils

import androidx.compose.ui.graphics.Color
import com.decagon.ui.theme.AppColors
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * UI-specific formatting utilities.
 * Formats values for display with proper styling.
 */
object UiFormatters {
    
    // ==================== Currency Formatting ====================
    
    /**
     * Formats a USD value with proper decimals and separators.
     * Examples: $1,234.56, $0.12, $0.0000123
     */
    fun formatUsd(value: Double): String {
        return when {
            value >= 1.0 -> {
                // Standard format: $1,234.56
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                formatter.maximumFractionDigits = 2
                formatter.minimumFractionDigits = 2
                formatter.format(value)
            }
            value >= 0.01 -> {
                // Small values: $0.12
                "%.2f".format(value)
            }
            value > 0 -> {
                // Very small: $0.000123 (show significant digits)
                val significantDigits = 6
                "$${"%.${significantDigits}f".format(value).trimEnd('0').trimEnd('.')}"
            }
            else -> "$0.00"
        }
    }

    /**
     * Format SOL amount (9 decimals).
     */
    fun formatSol(lamports: Long): String {
        val sol = lamports / 1_000_000_000.0
        return formatCryptoAmount(sol, "SOL")
    }
    
    /**
     * Formats a token amount with proper decimals.
     * Examples: 1,234.5678 SOL, 0.0000123 BTC
     */
    fun formatTokenAmount(amount: Double, symbol: String, maxDecimals: Int = 4): String {
        val formatted = when {
            amount >= 1.0 -> {
                // Large amounts: 1,234.5678
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                formatter.maximumFractionDigits = maxDecimals
                formatter.minimumFractionDigits = 0
                formatter.format(amount)
            }
            amount > 0 -> {
                // Small amounts: 0.0000123
                "%.${maxDecimals + 4}f".format(amount).trimEnd('0').trimEnd('.')
            }
            else -> "0"
        }
        return "$formatted $symbol"
    }
    
    /**
     * Formats a compact number (K, M, B, T).
     * Examples: 1.5K, 2.3M, 1.2B
     */
    fun formatCompactNumber(value: Double, decimals: Int = 1): String {
        return when {
            value >= 1_000_000_000_000 -> "${"%.${decimals}f".format(value / 1_000_000_000_000)}T"
            value >= 1_000_000_000 -> "${"%.${decimals}f".format(value / 1_000_000_000)}B"
            value >= 1_000_000 -> "${"%.${decimals}f".format(value / 1_000_000)}M"
            value >= 1_000 -> "${"%.${decimals}f".format(value / 1_000)}K"
            else -> "%.${decimals}f".format(value)
        }
    }
    
    // ==================== Percentage Formatting ====================
    
    /**
     * Formats a percentage with color coding.
     * Returns Pair<Color, String>
     */
    fun formatPercentageChange(changePercent: Double): Pair<Color, String> {
        val color = when {
            changePercent > 0 -> AppColors.Success
            changePercent < 0 -> AppColors.Error
            else -> AppColors.Neutral
        }
        
        val sign = if (changePercent > 0) "+" else ""
        val formatted = "$sign${"%.2f".format(changePercent)}%"
        
        return color to formatted
    }
    
    /**
     * Formats APY with color coding.
     */
    fun formatApy(apy: Double): Pair<Color, String> {
        val color = when {
            apy >= 10.0 -> AppColors.Success
            apy >= 5.0 -> AppColors.Warning
            else -> AppColors.Neutral
        }
        
        return color to "${"%.2f".format(apy)}%"
    }
    
    // ==================== Address Formatting ====================
    
    /**
     * Truncates an address for display.
     * Example: "AbC...xYz"
     */
    fun formatAddress(
        address: String,
        prefixLength: Int = 4,
        suffixLength: Int = 4
    ): String {
        if (address.length <= prefixLength + suffixLength) return address
        return "${address.take(prefixLength)}...${address.takeLast(suffixLength)}"
    }

    /**
     * Format transaction hash.
     */
    fun formatTxHash(hash: String): String = formatAddress(hash, 8, 8)

    /**
     * Format currency with proper decimals.
     */
    fun formatCurrency(amount: Double, currency: String = "USD"): String {
        val formatter = DecimalFormat("#,##0.00")
        return when (currency) {
            "USD" -> "$${formatter.format(amount)}"
            "EUR" -> "€${formatter.format(amount)}"
            "GBP" -> "£${formatter.format(amount)}"
            else -> "${formatter.format(amount)} $currency"
        }
    }

    /**
     * Format crypto amount with dynamic decimals.
     */
    fun formatCryptoAmount(amount: Double, symbol: String): String {
        val decimals = when {
            amount >= 1000 -> 2
            amount >= 1 -> 4
            amount >= 0.01 -> 6
            else -> 8
        }
        val formatter = DecimalFormat("#,##0.${"0".repeat(decimals)}")
        return "${formatter.format(amount)} $symbol"
    }

    // ==================== Time Formatting ====================
    
    /**
     * Formats a timestamp as relative time.
     * Examples: "Just now", "5m ago", "2h ago", "3d ago"
     */
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            diff < 2_592_000_000 -> "${diff / 604_800_000}w ago"
            else -> formatDate(timestamp)
        }
    }

    /**
     * Format timestamp to time string.
     */
    fun formatTime(timestamp: Long): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    /**
     * Format large numbers with K/M/B suffixes.
     */
    fun formatCompactNumber(value: Double): String {
        return when {
            value >= 1_000_000_000 -> "${"%.2f".format(value / 1_000_000_000)}B"
            value >= 1_000_000 -> "${"%.2f".format(value / 1_000_000)}M"
            value >= 1_000 -> "${"%.2f".format(value / 1_000)}K"
            else -> "%.2f".format(value)
        }
    }
    
    /**
     * Formats a timestamp as a date.
     * Example: "Jan 15, 2024"
     */
    fun formatDate(timestamp: Long, pattern: String = "MMM dd, yyyy"): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(pattern, Locale.US)
        return formatter.format(date)
    }
    
    // ==================== Rate Formatting ====================
    
    /**
     * Formats an exchange rate.
     * Example: "1 SOL ≈ 159.23 USDC"
     */
    fun formatExchangeRate(
        fromAmount: Double,
        fromSymbol: String,
        toAmount: Double,
        toSymbol: String
    ): String {
        val rate = toAmount / fromAmount
        return "1 $fromSymbol ≈ ${"%.2f".format(rate)} $toSymbol"
    }
    
    /**
     * Formats price impact.
     */
    fun formatPriceImpact(impact: Double): Pair<Color, String> {
        val color = when {
            impact < 1.0 -> AppColors.Success
            impact < 5.0 -> AppColors.Warning
            else -> AppColors.Error
        }
        
        return color to "${"%.2f".format(impact)}%"
    }
    
    // ==================== File Size Formatting ====================
    
    /**
     * Formats bytes to human-readable size.
     * Example: "1.23 MB"
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824 -> "${"%.2f".format(bytes / 1_073_741_824.0)} GB"
            bytes >= 1_048_576 -> "${"%.2f".format(bytes / 1_048_576.0)} MB"
            bytes >= 1_024 -> "${"%.2f".format(bytes / 1_024.0)} KB"
            else -> "$bytes B"
        }
    }
}