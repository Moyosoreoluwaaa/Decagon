package com.koin.data.coin

import android.Manifest
import android.util.Log
import androidx.annotation.RequiresPermission
import com.koin.data.coin.dto.PriceDataPoint
import com.koin.data.coin.dto.toPriceDataPoints
import com.koin.domain.coin.CoinRepository
import com.koin.domain.model.Coin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

class CoinRepositoryImpl (
    private val apiService: CoinGeckoApiService,
    private val coinDao: CoinDao,
    private val networkUtil: NetworkUtil
) : CoinRepository {

    private val _coins = MutableStateFlow<Map<String, Coin>>(emptyMap())
    val coins = _coins.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)

    override fun getAllCoins(): Flow<Result<List<Coin>>> =
        coins.map { Result.success(it.values.toList()) }
    override fun getCoinById(id: String?): Flow<Result<Coin?>> =
        coins.map { Result.success(it[id]) }

    init {
        CoroutineScope(Dispatchers.IO).launch @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE) {
            loadFromCache()
            if (networkUtil.isNetworkAvailable()) {
                refreshFromNetwork()
            }
        }
    }

    private suspend fun loadFromCache() {
        try {
            val cachedCoins = coinDao.getAllCoins().first()
            if (cachedCoins.isNotEmpty()) {
                _coins.value = cachedCoins.map { it.toDomain() }.associateBy { it.id }
            }
        } catch (e: Exception) {
            val errorMsg = "Failed to load coins from cache: ${e.localizedMessage}"
            Log.e("CoinRepositoryImpl", errorMsg, e)
            _lastError.value = errorMsg
        }
    }

    internal suspend fun refreshFromNetwork() {
        try {
            val freshData = apiService.getCoinsWithFullDetails()
            val domainCoins = freshData.map { it.toDomain() }
            _coins.value = domainCoins.associateBy { it.id }
            coinDao.deleteAllCoins()
            coinDao.insertAll(domainCoins.map { it.toEntity() })

            // Prefetch chart data for all coins and selected time ranges
            val selectedTimeRanges = listOf(TimeRange.ONE_DAY, TimeRange.ONE_WEEK, TimeRange.ONE_YEAR, TimeRange.ALL)
            domainCoins.forEach { coin ->
                selectedTimeRanges.forEach { range ->
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            getCoinMarketChart(coin.id, range, "usd")
                        } catch (_: Exception) { /* Ignore errors for prefetch */ }
                    }
                }
            }
        } catch (e: Exception) {
            val errorMsg = "Failed to refresh coins from network: ${e.localizedMessage}"
            Log.e("CoinRepositoryImpl", errorMsg, e)
            _lastError.value = errorMsg
            if (_coins.value.isEmpty()) {
                // Handle the case where there's no cached data
                val noCacheMsg = "No cached data available. Please check your internet connection."
                Log.e("CoinRepositoryImpl", noCacheMsg)
                _lastError.value = noCacheMsg
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override suspend fun refreshCoins() {
        if (networkUtil.isNetworkAvailable()) {
            refreshFromNetwork()
        }
    }

    // In-memory cache for chart data: (coinId, timeRange) -> List<PriceDataPoint>
    private val chartCache = mutableMapOf<Pair<String, TimeRange>, List<PriceDataPoint>>()

    override suspend fun getCoinMarketChart(
        coinId: String,
        timeRange: TimeRange,
        vsCurrency: String
    ): List<PriceDataPoint> {
        val cacheKey = coinId to timeRange
        chartCache[cacheKey]?.let { cached ->
            return cached
        }

        // Persistent cache: check DB first
        val cacheDurationMillis = 60 * 60 * 1000L // 1 hour (adjust to match your other coin data cache duration)
        val now = System.currentTimeMillis()
        val chartEntity = coinDao.getCoinChart(coinId, timeRange.name)
        if (chartEntity != null && (now - chartEntity.timestamp) < cacheDurationMillis) {
            // Parse JSON to List<Pair<Long, Double>>
            val chartList = Converters().toChartList(chartEntity.priceDataJson) ?: emptyList()
            val priceData = chartList.map { PriceDataPoint(it.first, it.second) }
            chartCache[cacheKey] = priceData
            return priceData
        }

        // Not cached or expired: fetch from network
        return try {
            val calendar = Calendar.getInstance()
            val endTime = calendar.timeInMillis / 1000 // Current time in seconds
            timeRange.days?.let { calendar.add(Calendar.DAY_OF_YEAR, -it) }
            val startTime = calendar.timeInMillis / 1000 // Start time in seconds

            val response = apiService.getCoinMarketChartRange(
                id = coinId,
                vsCurrency = vsCurrency,
                from = startTime,
                to = endTime
            )
            val data = response.toPriceDataPoints()
            chartCache[cacheKey] = data
            // Save to DB
            val chartList = data.map { it.timestamp to it.price }
            val entity = CoinChartEntity(
                coinId = coinId,
                timeRange = timeRange.name,
                timestamp = now,
                priceDataJson = Converters().fromChartList(chartList) ?: "[]"
            )
            coinDao.insertCoinChart(entity)
            data
        } catch (e: Exception) {
            Log.e("CoinRepositoryImpl", "Error fetching market chart: ${e.localizedMessage}", e)
            emptyList()
        }
    }
}
