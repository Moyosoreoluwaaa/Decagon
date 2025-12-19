package com.koin.domain.watchlist

data class WatchlistItem(
    val id: Long = 0,
    val userId: Long,
    val coinId: String,
    val coinName: String,
    val coinSymbol: String,
    val coinImageUrl: String,
    val addedAt: Long
)
