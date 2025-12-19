package com.koin.domain.coin

import com.koin.data.coin.TimeRange
import com.koin.data.coin.dto.PriceDataPoint
import com.koin.domain.model.Coin
import kotlinx.coroutines.flow.Flow
 
interface CoinRepository {
    fun getAllCoins(): Flow<Result<List<Coin>>>
    fun getCoinById(id: String?): Flow<Result<Coin?>>
    suspend fun refreshCoins()
    suspend fun getCoinMarketChart(
        coinId: String,
        timeRange: TimeRange,
        vsCurrency: String = "usd"
    ): List<PriceDataPoint>
}
