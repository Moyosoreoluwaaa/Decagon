package com.koin.domain.watchlist

import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getWatchlistForUser(userId: Long): Flow<List<WatchlistItem>>
    fun isInWatchlist(userId: Long, coinId: String): Flow<Boolean>
    suspend fun addToWatchlist(watchlistItem: WatchlistItem)
    suspend fun removeFromWatchlist(userId: Long, coinId: String)
}
