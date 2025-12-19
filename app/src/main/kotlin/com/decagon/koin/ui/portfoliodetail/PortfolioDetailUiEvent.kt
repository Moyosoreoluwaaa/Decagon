package com.koin.ui.portfoliodetail

import com.koin.data.coin.TimeRange

sealed class PortfolioDetailUiEvent {
    data object Refresh : PortfolioDetailUiEvent()
    data class TimeRangeSelected(val timeRange: TimeRange) : PortfolioDetailUiEvent()
    data class SellCoin(
        val coinId: String,
        val quantity: Double,
        val pricePerCoin: Double
    ) : PortfolioDetailUiEvent()
    data object ClearToast : PortfolioDetailUiEvent()
}