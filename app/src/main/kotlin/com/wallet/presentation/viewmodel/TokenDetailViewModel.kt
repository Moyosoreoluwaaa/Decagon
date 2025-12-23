package com.octane.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Token
import com.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

class TokenDetailViewModel(
    private val discoverRepository: DiscoverRepository
) : ViewModel() {

    private val TAG = "TokenDetailViewModel"

    private val _tokenDetail = MutableStateFlow<LoadingState<Token>>(LoadingState.Loading)
    val tokenDetail: StateFlow<LoadingState<Token>> = _tokenDetail.asStateFlow()

    private val _chartData = MutableStateFlow<LoadingState<List<Double>>>(LoadingState.Idle)
    val chartData: StateFlow<LoadingState<List<Double>>> = _chartData.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("1D")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    fun loadToken(tokenId: String, symbol: String) {
        viewModelScope.launch {
            // ✅ CRITICAL FIX: Use observeAllTokens() to search entire cache
            discoverRepository.observeAllTokens()
                .map { state ->
                    when (state) {
                        is LoadingState.Success -> {
                            Timber.tag(TAG).d("🔍 Searching ${state.data.size} tokens for: id='$tokenId', symbol='$symbol'")

                            val token = state.data.find {
                                it.id.equals(tokenId, ignoreCase = true) ||
                                        it.symbol.equals(symbol, ignoreCase = true)
                            }

                            if (token != null) {
                                Timber.tag(TAG).i("✅ Token found: ${token.name} (${token.symbol})")
                                LoadingState.Success(token)
                            } else {
                                Timber.tag(TAG).e("❌ Token NOT FOUND in ${state.data.size} cached tokens")
                                Timber.tag(TAG).d("Available tokens: ${state.data.map { "${it.symbol}(${it.id})" }}")
                                LoadingState.Error(
                                    IllegalArgumentException("Token not found"),
                                    "Token '$symbol' not found in cache"
                                )
                            }
                        }
                        is LoadingState.Loading -> {
                            Timber.tag(TAG).d("🔄 Loading tokens...")
                            LoadingState.Loading
                        }
                        is LoadingState.Error -> {
                            Timber.tag(TAG).e("❌ Error loading tokens: ${state.message}")
                            state
                        }
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

    fun formatPrice(priceUsd: Double): String {
        return when {
            priceUsd >= 1000 -> "$%.2fK".format(priceUsd / 1000)
            priceUsd >= 1 -> "$%.2f".format(priceUsd)
            priceUsd >= 0.01 -> "$%.4f".format(priceUsd)
            else -> "$%.6f".format(priceUsd)
        }
    }
}