// ==================== TokenDetailViewModel.kt ====================

package com.octane.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Token
import com.octane.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TokenDetailViewModel(
    private val discoverRepository: DiscoverRepository
) : ViewModel() {

    private val _tokenDetail = MutableStateFlow<LoadingState<Token>>(LoadingState.Loading)
    val tokenDetail: StateFlow<LoadingState<Token>> = _tokenDetail.asStateFlow()

    private val _chartData = MutableStateFlow<LoadingState<List<Double>>>(LoadingState.Idle)
    val chartData: StateFlow<LoadingState<List<Double>>> = _chartData.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("1D")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    // ✅ NEW: Pull-to-refresh state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ✅ NEW: Watchlist state
    private val _isInWatchlist = MutableStateFlow(false)
    val isInWatchlist: StateFlow<Boolean> = _isInWatchlist.asStateFlow()

    // ✅ NEW: Toast message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun loadToken(tokenId: String, symbol: String) {
        viewModelScope.launch {
            discoverRepository.observeTokens()
                .map { state ->
                    when (state) {
                        is LoadingState.Success -> {
                            val token = state.data.find {
                                it.id == tokenId || it.symbol.equals(symbol, ignoreCase = true)
                            }
                            if (token != null) {
                                LoadingState.Success(token)
                            } else {
                                LoadingState.Error(
                                    IllegalArgumentException("Token not found"),
                                    "Token not found in cache"
                                )
                            }
                        }
                        is LoadingState.Loading -> LoadingState.Loading
                        is LoadingState.Error -> state
                        else -> LoadingState.Loading
                    }
                }
                .collect {
                    _tokenDetail.value = it
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

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                // Trigger repository refresh
                // discoverRepository.refreshTokens()

                // Re-fetch chart
                fetchChartData(_selectedTimeframe.value)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun toggleWatchlist() {
        viewModelScope.launch {
            val token = (_tokenDetail.value as? LoadingState.Success)?.data ?: return@launch

            // TODO: Implement with WatchlistRepository
            _isInWatchlist.value = !_isInWatchlist.value
            _toastMessage.value = if (_isInWatchlist.value) {
                "${token.name} added to watchlist"
            } else {
                "${token.name} removed from watchlist"
            }
        }
    }

    fun setPriceAlert() {
        val token = (_tokenDetail.value as? LoadingState.Success)?.data ?: return
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
                kotlinx.coroutines.delay(1000)

                val mockData = List(50) {
                    100.0 + (Math.random() * 20 - 10)
                }

                _chartData.value = LoadingState.Success(mockData)
            } catch (e: Exception) {
                _chartData.value = LoadingState.Error(e, "Failed to load chart")
            }
        }
    }
}
