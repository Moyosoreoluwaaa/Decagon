package com.decagon.core.util

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

/**
 * Format as currency with proper thousands separator.
 * Example: 1234.56 → "$1,234.56"
 */
fun Double.formatCurrency(decimals: Int = 2): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = decimals
        minimumFractionDigits = decimals
    }
    return formatter.format(this).replace("$", "")
}

/**
 * Format large numbers compactly (1.5K, 2.3M, 1.2B).
 * Example: 1500.0 → "1.5K"
 */
fun Double.formatCompact(decimals: Int = 2): String {
    return when {
        this >= 1_000_000_000_000 -> "${"%.${decimals}f".format(this / 1_000_000_000_000)}T"
        this >= 1_000_000_000 -> "${"%.${decimals}f".format(this / 1_000_000_000)}B"
        this >= 1_000_000 -> "${"%.${decimals}f".format(this / 1_000_000)}M"
        this >= 1_000 -> "${"%.${decimals}f".format(this / 1_000)}K"
        else -> "${"%.${decimals}f".format(this)}"
    }
}

/**
 * Format as percentage with sign.
 * Example: 0.1523 → "+15.23%"
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
    val value = "%.${decimals}f".format(this)
    return "$sign$value%"
}