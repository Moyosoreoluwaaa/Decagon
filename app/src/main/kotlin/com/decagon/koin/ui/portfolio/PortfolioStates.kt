package com.koin.ui.portfolio

import com.koin.data.coin.TimeRange
import com.koin.domain.portfolio.Portfolio
import com.koin.ui.portfolio.BuyTransactionDetails

data class PortfolioUiState(
    val isLoading: Boolean = false,
    val portfolio: Portfolio = Portfolio.Companion.empty(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val showBuyDialog: Boolean = false,
    val selectedCoinForBuy: String? = null,
    val showBottomSheet: Boolean = false,
    val selectedTimeRange: TimeRange = TimeRange.ALL,
    val showBuySuccessBottomSheet: Boolean = false,
    val buyTransactionDetails: BuyTransactionDetails? = null
)

sealed class PortfolioUiEvent {
    object RefreshData : PortfolioUiEvent()
    object ShowBottomSheet : PortfolioUiEvent()
    object HideBottomSheet : PortfolioUiEvent()
    object HideBuyDialog : PortfolioUiEvent()
    object HideBuySuccessBottomSheet : PortfolioUiEvent()
    data class ShowBuyDialog(val coinId: String) : PortfolioUiEvent()
    data class BuyCoin(
        val coinId: String,
        val quantity: Double
    ) : PortfolioUiEvent()
    data class SellCoin(
        val coinId: String,
        val quantity: Double,
        val pricePerCoin: Double
    ) : PortfolioUiEvent()
    data class SelectTimeRange(val timeRange: TimeRange) : PortfolioUiEvent()
}
