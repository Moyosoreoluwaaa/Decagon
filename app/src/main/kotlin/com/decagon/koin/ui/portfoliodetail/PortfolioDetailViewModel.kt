package com.koin.ui.portfoliodetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.koin.domain.coin.CoinRepository
import com.koin.domain.portfolio.PortfolioRepository
import com.koin.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PortfolioDetailViewModel (
    private val portfolioRepository: PortfolioRepository,
    private val coinRepository: CoinRepository,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<PortfolioDetailUiState, PortfolioDetailUiEvent>() {

    private val coinId: String = checkNotNull(savedStateHandle["coinId"])

    override val _uiState: MutableStateFlow<PortfolioDetailUiState> =
        MutableStateFlow(PortfolioDetailUiState(isLoading = true))

    

    init {
        loadHistoricalData()
        loadPortfolioCoin()
    }

    override fun handleEvent(event: PortfolioDetailUiEvent) {
        when (event) {
            is PortfolioDetailUiEvent.Refresh -> {
                _uiState.update { it.copy(isLoading = true) }
                loadPortfolioCoin()
                loadHistoricalData()
            }
            is PortfolioDetailUiEvent.TimeRangeSelected -> {
                _uiState.update { it.copy(selectedTimeRange = event.timeRange) }
                loadHistoricalData()
            }
            is PortfolioDetailUiEvent.ClearToast -> {
                // Not used yet, but kept for consistency
            }
            is PortfolioDetailUiEvent.SellCoin -> {
                sellCoin(event.coinId, event.quantity, event.pricePerCoin)
            }
        }
    }

    private fun sellCoin(coinId: String, quantity: Double, pricePerCoin: Double) {
        viewModelScope.launch {
            try {
                portfolioRepository.sellCoin(coinId, quantity, pricePerCoin)
                _uiState.update { it.copy(
                    transactionSuccess = true,
                    error = null,
                    soldCoinDetails = SellTransactionDetails(
                        coinName = it.portfolioCoin?.coinName ?: "",
                        quantity = quantity,
                        totalPrice = quantity * pricePerCoin
                    )
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to sell coin: ${e.localizedMessage}") }
            }
        }
    }

    private fun loadPortfolioCoin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            portfolioRepository.getHoldings()
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collectLatest { holdings ->
                    val portfolioCoin = holdings.find { it.coinId == coinId }
                    _uiState.update { it.copy(portfolioCoin = portfolioCoin, isLoading = false, error = null) }
                }
        }
    }

    private fun loadHistoricalData() {
        viewModelScope.launch {
            val currentPortfolioCoin = _uiState.value.portfolioCoin
            if (currentPortfolioCoin == null) {
                // If portfolioCoin is not loaded yet, wait for it or handle error
                return@launch
            }

            _uiState.update { it.copy(isLoadingHistoricalData = true) }

            try {
                val timeRange = _uiState.value.selectedTimeRange
                val historicalData = coinRepository.getCoinMarketChart(
                    coinId = currentPortfolioCoin.coinId,
                    timeRange = timeRange,
                    vsCurrency = "usd"
                )

                // Calculate portfolio value based on historical coin prices and holding quantity
                val portfolioHistoricalData = historicalData.map { priceDataPoint ->
                    priceDataPoint.copy(price = priceDataPoint.price * currentPortfolioCoin.quantity)
                }

                _uiState.update { state ->
                    state.copy(
                        historicalData = portfolioHistoricalData,
                        isLoadingHistoricalData = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        error = e.message,
                        isLoadingHistoricalData = false,
                        historicalData = emptyList() // Clear any previous data on error
                    )
                }
            }
        }
    }
}
