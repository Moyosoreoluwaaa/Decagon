package com.decagon.ui.screen.perps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.domain.model.Perp
import com.decagon.core.util.LoadingState
import com.decagon.domain.repository.DiscoverRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class PerpDetailViewModel(
    private val discoverRepository: DiscoverRepository
) : ViewModel() {

    private val TAG = "PerpDetailViewModel"

    private val _perpDetail = MutableStateFlow<LoadingState<Perp>>(LoadingState.Loading)
    val perpDetail: StateFlow<LoadingState<Perp>> = _perpDetail.asStateFlow()

    private val _chartData = MutableStateFlow<LoadingState<List<Double>>>(LoadingState.Idle)
    val chartData: StateFlow<LoadingState<List<Double>>> = _chartData.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("1D")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _selectedLeverage = MutableStateFlow(10)
    val selectedLeverage: StateFlow<Int> = _selectedLeverage.asStateFlow()

    // ✅ NEW: Pull-to-refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ✅ NEW: Watchlist state
    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist: StateFlow<Boolean> = _isInWatchlist.asStateFlow()

    // ✅ NEW: Toast message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun loadPerp(perpSymbol: String) {
        viewModelScope.launch {
            discoverRepository.observeAllPerps()
                .map { state ->
                    when (state) {
                        is LoadingState.Success -> {
                            Timber.tag(TAG).d("🔍 Searching ${state.data.size} perps for: symbol='$perpSymbol'")

                            val perp = state.data.find {
                                it.symbol.equals(perpSymbol, ignoreCase = true)
                            }

                            if (perp != null) {
                                Timber.tag(TAG).i("✅ Perp found: ${perp.name} (${perp.symbol})")
                                LoadingState.Success(perp)
                            } else {
                                Timber.tag(TAG).e("❌ Perp NOT FOUND in ${state.data.size} cached perps")
                                LoadingState.Error(
                                    IllegalArgumentException("Perp not found"),
                                    "Perp '$perpSymbol' not found in cache"
                                )
                            }
                        }
                        is LoadingState.Loading -> LoadingState.Loading
                        is LoadingState.Error -> state
                        else -> LoadingState.Loading
                    }
                }
                .collect {
                    _perpDetail.value = it
                    if (it is LoadingState.Success && _chartData.value is LoadingState.Idle) {
                        fetchChartData(_selectedTimeframe.value)
                    }
                }
        }
    }

    fun onTimeframeSelected(timeframe: String) {
        if (_selectedTimeframe.value == timeframe) return
        _selectedTimeframe.value = timeframe
        fetchChartData(timeframe)
    }

    fun onLeverageSelected(leverage: Int) {
        _selectedLeverage.value = leverage
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                // TODO: Trigger repository refresh
                // discoverRepository.refreshPerps()
                
                // Re-fetch chart
                fetchChartData(_selectedTimeframe.value)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            val perp = (_perpDetail.value as? LoadingState.Success)?.data ?: return@launch
            
            // TODO: Implement with WatchlistRepository
            _isInWatchlist.value = !_isInWatchlist.value
            _toastMessage.value = if (_isInWatchlist.value) {
                "${perp.name} added to watchlist"
            } else {
                "${perp.name} removed from watchlist"
            }
        }
    }

    fun setPriceAlert() {
        val perp = (_perpDetail.value as? LoadingState.Success)?.data ?: return
        // TODO: Show price alert dialog
        _toastMessage.value = "Price alert feature coming soon"
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun fetchChartData(timeframe: String) {
        viewModelScope.launch {
            _chartData.value = LoadingState.Loading

            try {
                delay(1000)

                val mockData = List(50) {
                    1000.0 + (Math.random() * 100 - 50)
                }

                _chartData.value = LoadingState.Success(mockData)
            } catch (e: Exception) {
                _chartData.value = LoadingState.Error(e, "Failed to load chart")
            }
        }
    }
}
