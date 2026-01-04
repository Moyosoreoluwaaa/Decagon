package com.decagon.core.util

fun formatTimeout(seconds: String): String {
    return when (seconds.toIntOrNull() ?: 300) {
        60 -> "1 minute"
        300 -> "5 minutes"
        900 -> "15 minutes"
        1800 -> "30 minutes"
        3600 -> "1 hour"
        else -> "$seconds seconds"
    }
}