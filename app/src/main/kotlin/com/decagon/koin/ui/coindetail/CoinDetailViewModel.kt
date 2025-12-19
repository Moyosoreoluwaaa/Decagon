package com.koin.ui.coindetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.koin.app.notification.NotificationService
import com.koin.data.coin.TimeRange
import com.koin.data.coin.dto.PriceDataPoint
import com.koin.domain.coin.CoinRepository
import com.koin.domain.model.Coin
import com.koin.domain.user.UserRepository
import com.koin.domain.watchlist.WatchlistItem
import com.koin.domain.watchlist.WatchlistRepository
import com.koin.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class CoinDetailUiEvent {
    object Refresh : CoinDetailUiEvent()
    data class TimeRangeSelected(val timeRange: TimeRange) : CoinDetailUiEvent()
    object ToggleWatchlist : CoinDetailUiEvent()
    object ClearToast : CoinDetailUiEvent()
    object SetPriceAlert : CoinDetailUiEvent()
    object DismissPriceAlertDialog : CoinDetailUiEvent()
}

class CoinDetailViewModel (
    private val repository: CoinRepository,
    private val userRepository: UserRepository,
    private val watchlistRepository: WatchlistRepository,
    private val notificationService: NotificationService,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<CoinDetailUiState, CoinDetailUiEvent>() {

    private val coinId: String = checkNotNull(savedStateHandle["coinId"])

    override val _uiState: MutableStateFlow<CoinDetailUiState> =
        MutableStateFlow(CoinDetailUiState(coinId = coinId))

    init {
        // Initial load should not set isRefreshing to true for a 'pull' action,
        // but rather isLoading. isRefreshing is specifically for pull-to-refresh.
        loadCoin(isRefresh = false)
        loadHistoricalData(isRefresh = false)
        observeWatchlistStatus()
    }

    override fun handleEvent(event: CoinDetailUiEvent) {
        when (event) {
            is CoinDetailUiEvent.Refresh -> {
                // When a Refresh event comes from pull-to-refresh, set isRefreshing to true
                _uiState.update { it.copy(isRefreshing = true) }
                loadCoin(isRefresh = true)
                loadHistoricalData(isRefresh = true)
            }
            is CoinDetailUiEvent.TimeRangeSelected -> {
                _uiState.update { it.copy(selectedTimeRange = event.timeRange) }
                loadHistoricalData(isRefresh = false) // Time range change is not a "refresh"
            }
            is CoinDetailUiEvent.ToggleWatchlist -> {
                toggleWatchlist()
            }
            is CoinDetailUiEvent.ClearToast -> {
                _uiState.update { it.copy(toastMessage = null) }
            }
            is CoinDetailUiEvent.SetPriceAlert -> {
                _uiState.update { it.copy(showPriceAlertDialog = true) }
            }
            is CoinDetailUiEvent.DismissPriceAlertDialog -> {
                _uiState.update { it.copy(showPriceAlertDialog = false) }
            }
        }
    }

    private fun loadCoin(isRefresh: Boolean = false) {
        viewModelScope.launch {
            // Only set isLoading if it's not a refresh (initial load or explicit non-refresh)
            // isRefreshing handles the loading state for pull-to-refresh
            if (!isRefresh) {
                _uiState.update { it.copy(isLoading = true) }
            }

            repository.getCoinById(coinId)
                .catch { e ->
                    _uiState.update { state ->
                        state.copy(
                            error = e.message,
                            isLoading = false,
                            isRefreshing = false // Always reset isRefreshing on completion
                        )
                    }
                    //Timber.e(e, "Error loading coin details")
                }
                .collectLatest { result ->
                    result.onSuccess { coin ->
                        _uiState.update { state ->
                            state.copy(
                                coin = coin,
                                isLoading = false,
                                isRefreshing = false, // Always reset isRefreshing on completion
                                error = null
                            )
                        }
                    }.onFailure { e ->
                        _uiState.update { state ->
                            state.copy(
                                error = e.message,
                                isLoading = false,
                                isRefreshing = false // Always reset isRefreshing on completion
                            )
                        }
                    }
                }
        }
    }

    private fun loadHistoricalData(isRefresh: Boolean = false) {
        viewModelScope.launch {
            // Only set isLoadingHistoricalData if it's not a refresh
            if (!isRefresh) {
                _uiState.update { it.copy(isLoadingHistoricalData = true) }
            }

            try {
                val timeRange = _uiState.value.selectedTimeRange
                val historicalData = repository.getCoinMarketChart(
                    coinId = coinId,
                    timeRange = timeRange,
                    vsCurrency = "usd"
                )

                _uiState.update { state ->
                    state.copy(
                        historicalData = historicalData,
                        isLoadingHistoricalData = false,
                        isRefreshing = false, // Always reset isRefreshing on completion
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoadingHistoricalData = false,
                        historicalData = emptyList(), // Clear any previous data on error
                        isRefreshing = false // Always reset isRefreshing on completion
                    )
                }
//                Timber.e(e, "Error loading historical data")
            }
        }
    }

    private fun observeWatchlistStatus() {
        viewModelScope.launch {
            // Get the first user (assuming single user app for now)
            userRepository.users().collectLatest { users ->
                if (users.isNotEmpty()) {
                    val user = users.first()
                    watchlistRepository.isInWatchlist(user.id, coinId).collectLatest { isInWatchlist ->
                        _uiState.update { it.copy(isInWatchlist = isInWatchlist, currentUserId = user.id) }
                    }
                }
            }
        }
    }

    private fun toggleWatchlist() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val userId = currentState.currentUserId ?: return@launch
            val coin = currentState.coin ?: return@launch

            try {
                if (currentState.isInWatchlist) {
                    watchlistRepository.removeFromWatchlist(userId, coinId)
                    _uiState.update { it.copy(toastMessage = "${coin.name} removed from watchlist") }
                } else {
                    val watchlistItem = WatchlistItem(
                        userId = userId,
                        coinId = coin.id,
                        coinName = coin.name,
                        coinSymbol = coin.symbol,
                        coinImageUrl = coin.imageUrl,
                        addedAt = System.currentTimeMillis()
                    )
                    watchlistRepository.addToWatchlist(watchlistItem)
                    _uiState.update { it.copy(toastMessage = "${coin.name} added to watchlist") }
                    notificationService.showWatchlistNotification(coin.name)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update watchlist: ${e.message}") }
            }
        }
    }
}

data class CoinDetailUiState(
    val coinId: String,
    val coin: Coin? = null,
    val historicalData: List<PriceDataPoint> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingHistoricalData: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedTimeRange: TimeRange = TimeRange.ONE_DAY,
    val isInWatchlist: Boolean = false,
    val currentUserId: Long? = null,
    val toastMessage: String? = null,
    val showPriceAlertDialog: Boolean = false
)
