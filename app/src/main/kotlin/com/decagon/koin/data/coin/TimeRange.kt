package com.koin.data.coin

enum class TimeRange(val days: Int?, val displayName: String) {
    // 'days' is now nullable for 'ALL'

    ONE_DAY(1, "1D"),
    ONE_WEEK(7, "1W"),
    ONE_YEAR(365, "1Y"),
    ALL(null, "All"); // Null days for 'All', will need special handling in repo

    // You might need a helper function to get appropriate `from` based on timeRange
    fun getStartTimeSeconds(endTimeSeconds: Long): Long {
        return when (this) {
            ONE_DAY -> endTimeSeconds - (24 * 60 * 60) // 1 day ago
            ONE_WEEK -> endTimeSeconds - (7 * 24 * 60 * 60) // 7 days ago
            ONE_YEAR -> endTimeSeconds - (365 * 24 * 60 * 60) // 365 days ago
            ALL -> 0 // Or some very early date for "all time" (CoinGecko typically uses 0 for ALL)
        }
    }
}