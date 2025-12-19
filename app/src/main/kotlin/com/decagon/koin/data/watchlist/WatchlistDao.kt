package com.koin.data.watchlist

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist WHERE userId = :userId ORDER BY addedAt DESC")
    fun getWatchlistForUser(userId: Long): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE userId = :userId AND coinId = :coinId LIMIT 1")
    fun getWatchlistItem(userId: Long, coinId: String): Flow<WatchlistEntity?>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE userId = :userId AND coinId = :coinId)")
    fun isInWatchlist(userId: Long, coinId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(watchlistEntity: WatchlistEntity)

    @Delete
    suspend fun delete(watchlistEntity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE userId = :userId AND coinId = :coinId")
    suspend fun deleteByUserAndCoin(userId: Long, coinId: String)

    @Query("DELETE FROM watchlist WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: Long)
}
