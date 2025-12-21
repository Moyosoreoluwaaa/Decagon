package com.wallet.core.extension

import kotlin.math.pow
import kotlin.math.round

/**
 * Format as SOL amount.
 */
fun Double.formatSOL(decimals: Int = 4): String {
    return "◎${"%.${decimals}f".format(this)}"
}

/**
 * Format as USD currency.
 */
fun Double.formatUSD(decimals: Int = 2): String {
    return "$${"%.${decimals}f".format(this)}"
}

/**
 * Format large numbers compactly (1.5K, 2.3M).
 */
fun Double.formatCompact(decimals: Int = 2): String {
    return when {
        this >= 1_000_000_000 -> "${"%.${decimals}f".format(this / 1_000_000_000)}B"
        this >= 1_000_000 -> "${"%.${decimals}f".format(this / 1_000_000)}M"
        this >= 1_000 -> "${"%.${decimals}f".format(this / 1_000)}K"
        else -> "%.${decimals}f".format(this)
    }
}

/**
 * Format as percentage.
 */
fun Double.formatPercentage(
    decimals: Int = 2,
    showSign: Boolean = true
): String {
    val sign = when {
        !showSign -> ""
        this >= 0 -> "+"
        else -> ""
    }
    val value = "%.${decimals}f".format(this * 100)
    return "$sign$value%"
}

/**
 * Round to specific decimal places.
 */
fun Double.roundTo(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return round(this * factor) / factor
}

/**
 * Clamp value between min and max.
 */
fun Double.clamp(min: Double, max: Double): Double =
    coerceIn(min, max)

fun Int.clamp(min: Int, max: Int): Int =
    coerceIn(min, max)

/**
 * Convert lamports to SOL (1 SOL = 1B lamports).
 */
fun Long.lamportsToSOL(): Double {
    return this / 1_000_000_000.0
}

/**
 * Convert SOL to lamports.
 */
fun Double.solToLamports(): Long {
    return (this * 1_000_000_000).toLong()
}

/**
 * Format bytes to human-readable format.
 */
fun Long.formatBytes(): String {
    return when {
        this >= 1_073_741_824 -> "${"%.2f".format(this / 1_073_741_824.0)} GB"
        this >= 1_048_576 -> "${"%.2f".format(this / 1_048_576.0)} MB"
        this >= 1_024 -> "${"%.2f".format(this / 1_024.0)} KB"
        else -> "$this B"
    }
}
