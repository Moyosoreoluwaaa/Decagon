package com.koin.data.watchlist

import com.koin.domain.watchlist.WatchlistItem
import com.koin.domain.watchlist.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WatchlistRepositoryImpl (
    private val watchlistDao: WatchlistDao
) : WatchlistRepository {

    override fun getWatchlistForUser(userId: Long): Flow<List<WatchlistItem>> =
        watchlistDao.getWatchlistForUser(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun isInWatchlist(userId: Long, coinId: String): Flow<Boolean> =
        watchlistDao.isInWatchlist(userId, coinId)

    override suspend fun addToWatchlist(watchlistItem: WatchlistItem) {
        watchlistDao.insert(watchlistItem.toEntity())
    }

    override suspend fun removeFromWatchlist(userId: Long, coinId: String) {
        watchlistDao.deleteByUserAndCoin(userId, coinId)
    }
}
