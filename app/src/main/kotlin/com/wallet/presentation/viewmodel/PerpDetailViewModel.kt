package com.octane.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Perp
import com.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PerpDetailViewModel(
    private val discoverRepository: DiscoverRepository
) : ViewModel() {
    
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
            discoverRepository.observePerps()
                .map { state ->
                    when (state) {
                        is LoadingState.Success -> {
                            val perp = state.data.find { 
                                it.symbol.equals(perpSymbol, ignoreCase = true) 
                            }
                            if (perp != null) {
                                LoadingState.Success(perp)
                            } else {
                                LoadingState.Error(
                                    IllegalArgumentException("Perp not found"),
                                    "Perp not found in cache"
                                )
                            }
                        }
                        is LoadingState.Loading -> LoadingState.Loading
                        is LoadingState.Error -> state
                        else -> LoadingState.Loading
                    }
                }
                .collect { _perpDetail.value = it
                    // ✅ Auto-fetch chart on successful load
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
                kotlinx.coroutines.delay(1000)
                
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