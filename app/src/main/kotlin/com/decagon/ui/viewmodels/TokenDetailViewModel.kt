package com.decagon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Token
import com.decagon.domain.repository.DiscoverRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
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

    // ✅ Reactive observation of token from cache
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
                    // ✅ Auto-fetch chart on successful load
                    if (it is LoadingState.Success && _chartData.value is LoadingState.Idle) {
                        fetchChartData(_selectedTimeframe.value)
                    }
                }
        }
    }

    // ✅ One-shot chart fetch (only when timeframe changes)
    fun onTimeframeSelected(timeframe: String) {
        if (_selectedTimeframe.value == timeframe) return // Skip if same

        _selectedTimeframe.value = timeframe
        fetchChartData(timeframe)
    }

    private fun fetchChartData(timeframe: String) {
        viewModelScope.launch {
            _chartData.value = LoadingState.Loading

            try {
                // TODO: Replace with actual chart API call
                delay(1000) // Simulate network

                val mockData = List(50) {
                    100.0 + (Math.random() * 20 - 10)
                }

                _chartData.value = LoadingState.Success(mockData)
            } catch (e: Exception) {
                _chartData.value = LoadingState.Error(e, "Failed to load chart")
            }
        }
    }

    fun formatPrice(priceUsd: Double): String {
        return when {
            priceUsd >= 1000 -> "$%.2fK".format(priceUsd / 1000)
            priceUsd >= 1 -> "$%.2f".format(priceUsd)
            priceUsd >= 0.01 -> "$%.4f".format(priceUsd)
            else -> "$%.6f".format(priceUsd)
        }
    }
}