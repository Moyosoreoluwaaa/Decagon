package com.koin.data.coin

import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface CoinDao {
    @Query("SELECT * FROM coins ORDER BY marketCapRank ASC")
    fun getAllCoins(): Flow<List<CoinEntity>>

    @Query("SELECT * FROM coins WHERE id = :coinId")
    fun getCoinById(coinId: String): Flow<CoinEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coins: List<CoinEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoin(coin: CoinEntity)

    @Query("DELETE FROM coins")
    suspend fun deleteAllCoins()

    @Query("SELECT COUNT(*) FROM coins")
    suspend fun getCoinCount(): Int

    // Chart data methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCoinChart(chart: CoinChartEntity)

    @Query("SELECT * FROM coin_chart WHERE coinId = :coinId AND timeRange = :timeRange ORDER BY timestamp DESC LIMIT 1")
    suspend fun getCoinChart(coinId: String, timeRange: String): CoinChartEntity?

    @Query("DELETE FROM coin_chart WHERE coinId = :coinId AND timeRange = :timeRange")
    suspend fun deleteCoinChart(coinId: String, timeRange: String)

    @Query("DELETE FROM coin_chart WHERE timestamp < :cutoff")
    suspend fun pruneOldCharts(cutoff: Long)
}

