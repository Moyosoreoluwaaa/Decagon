package com.wallet.core.extension

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Format timestamp as relative time.
 */
fun Long.formatRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> formatDate()
    }
}

/**
 * Format timestamp as date.
 */
fun Long.formatDate(pattern: String = "MMM dd, yyyy"): String {
    // TODO: Use kotlinx-datetime for KMP
    return SimpleDateFormat(pattern, Locale.US)
        .format(Date(this))
}

/**
 * Format timestamp as time.
 */
fun Long.formatTime(use24Hour: Boolean = false): String {
    val pattern = if (use24Hour) "HH:mm" else "hh:mm a"
    return SimpleDateFormat(pattern, Locale.US)
        .format(Date(this))
}

/**
 * Check if timestamp is today.
 */
fun Long.isToday(): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = this@isToday }
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

/**
 * Check if timestamp is yesterday.
 */
fun Long.isYesterday(): Boolean {
    val yesterday = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }
    val date = Calendar.getInstance().apply { timeInMillis = this@isYesterday }
    return yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}
