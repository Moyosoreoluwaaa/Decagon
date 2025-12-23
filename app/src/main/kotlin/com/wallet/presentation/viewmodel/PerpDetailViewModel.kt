package com.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.wallet.domain.models.Perp
import com.wallet.core.util.LoadingState
import com.wallet.domain.repository.DiscoverRepository
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

    fun loadPerp(perpSymbol: String) {
        viewModelScope.launch {
            // ✅ CRITICAL FIX: Use observeAllPerps() to search entire cache
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
                                Timber.tag(TAG).d("Available perps: ${state.data.map { it.symbol }}")
                                LoadingState.Error(
                                    IllegalArgumentException("Perp not found"),
                                    "Perp '$perpSymbol' not found in cache"
                                )
                            }
                        }
                        is LoadingState.Loading -> {
                            Timber.tag(TAG).d("🔄 Loading perps...")
                            LoadingState.Loading
                        }
                        is LoadingState.Error -> {
                            Timber.tag(TAG).e("❌ Error loading perps: ${state.message}")
                            state
                        }
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