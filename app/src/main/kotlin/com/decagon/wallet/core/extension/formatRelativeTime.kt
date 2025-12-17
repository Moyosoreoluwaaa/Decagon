package com.octane.wallet.core.extension

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
    return java.text.SimpleDateFormat(pattern, java.util.Locale.US)
        .format(java.util.Date(this))
}

/**
 * Format timestamp as time.
 */
fun Long.formatTime(use24Hour: Boolean = false): String {
    val pattern = if (use24Hour) "HH:mm" else "hh:mm a"
    return java.text.SimpleDateFormat(pattern, java.util.Locale.US)
        .format(java.util.Date(this))
}

/**
 * Check if timestamp is today.
 */
fun Long.isToday(): Boolean {
    val today = java.util.Calendar.getInstance()
    val date = java.util.Calendar.getInstance().apply { timeInMillis = this@isToday }
    return today.get(java.util.Calendar.YEAR) == date.get(java.util.Calendar.YEAR) &&
            today.get(java.util.Calendar.DAY_OF_YEAR) == date.get(java.util.Calendar.DAY_OF_YEAR)
}

/**
 * Check if timestamp is yesterday.
 */
fun Long.isYesterday(): Boolean {
    val yesterday = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.DAY_OF_YEAR, -1)
    }
    val date = java.util.Calendar.getInstance().apply { timeInMillis = this@isYesterday }
    return yesterday.get(java.util.Calendar.YEAR) == date.get(java.util.Calendar.YEAR) &&
            yesterday.get(java.util.Calendar.DAY_OF_YEAR) == date.get(java.util.Calendar.DAY_OF_YEAR)
}
