package com.decagon.core.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Formatting utilities for Octane Wallet.
 * Handles SOL amounts, NFT prices, wallet addresses, etc.
 */
object Formatters {

    /**
     * Format SOL amount with proper decimals (max 9).
     * @param amount SOL amount (e.g., 1.23456789)
     * @param decimals Number of decimals to show (default: 4)
     * @param showSymbol Include ◎ symbol
     */
    fun formatSOL(
        amount: Double,
        decimals: Int = 4,
        showSymbol: Boolean = true
    ): String {
        val symbol = if (showSymbol) "◎" else ""
        return "$symbol${"%.${decimals}f".format(amount)}"
    }

    /**
     * Format NFT floor price.
     * Adjusts decimals based on magnitude.
     */
    fun formatFloorPrice(price: Double): String {
        return when {
            price >= 1000 -> "◎${formatCompact(price)}"
            price >= 1 -> "◎${"%.2f".format(price)}"
            price >= 0.01 -> "◎${"%.4f".format(price)}"
            else -> "◎${"%.6f".format(price)}"
        }
    }

    /**
     * Format wallet address with truncation.
     * Standard pattern: abcd...wxyz
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
     * Format transaction priority fee.
     * @param microLamports Fee in microLamports (1 SOL = 1B lamports)
     */
    fun formatPriorityFee(microLamports: Long): String {
        val sol = microLamports / 1_000_000_000.0
        return "≈◎${"%.6f".format(sol)}"
    }

    /**
     * Format large numbers compactly (1.5K, 2.3M, 1.2B).
     */
    fun formatCompact(number: Double, decimals: Int = 2): String {
        return when {
            number >= 1_000_000_000_000 ->
                "${"%.${decimals}f".format(number / 1_000_000_000_000)}T"
            number >= 1_000_000_000 ->
                "${"%.${decimals}f".format(number / 1_000_000_000)}B"
            number >= 1_000_000 ->
                "${"%.${decimals}f".format(number / 1_000_000)}M"
            number >= 1_000 ->
                "${"%.${decimals}f".format(number / 1_000)}K"
            else ->
                "%.${decimals}f".format(number)
        }
    }

    /**
     * Format percentage with sign.
     */
    fun formatPercentage(
        value: Double,
        decimals: Int = 2,
        showSign: Boolean = true
    ): String {
        val sign = when {
            !showSign -> ""
            value >= 0 -> "+"
            else -> ""
        }
        val formatted = "%.${decimals}f".format(value * 100)
        return "$sign$formatted%"
    }

    /**
     * Format currency (USD, EUR, etc.).
     */
    fun formatCurrency(
        amount: Double,
        symbol: String = "$",
        decimals: Int = 2
    ): String {
        val formatted = "%.${decimals}f".format(amount)
        return "$symbol$formatted"
    }

    /**
     * Format relative time (2m ago, 1h ago, etc.).
     */
    fun formatRelativeTime(timestampMs: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestampMs

        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> formatDate(timestampMs)
        }
    }

    /**
     * Format timestamp as date.
     */
    fun formatDate(timestampMs: Long, pattern: String = "MMM dd, yyyy"): String {
        // TODO: Use kotlinx-datetime for KMP compatibility
        // For now, return ISO format
        return SimpleDateFormat(pattern, Locale.US)
            .format(Date(timestampMs))
    }

    /**
     * Format transaction hash (truncate).
     */
    fun formatTxHash(hash: String): String = formatAddress(hash, 8, 8)
}
