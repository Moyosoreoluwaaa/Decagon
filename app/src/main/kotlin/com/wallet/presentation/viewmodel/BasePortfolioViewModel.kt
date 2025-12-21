package com.wallet.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.wallet.core.network.ConnectionType
import com.octane.wallet.core.network.NetworkMonitor
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.usecases.asset.ObservePortfolioUseCase
import com.octane.wallet.domain.usecases.asset.PortfolioState
import com.octane.wallet.domain.usecases.asset.RefreshAssetsUseCase
import com.octane.wallet.domain.usecases.asset.ToggleAssetVisibilityUseCase
import com.octane.wallet.domain.usecases.preference.ObserveCurrencyPreferenceUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Shared portfolio state management.
 * Used by: HomeScreen, WalletsScreen, PortfolioWidget
 *
 * RESPONSIBILITY:
 * - Aggregate portfolio value across all wallets
 * - Real-time balance updates
 * - Asset visibility toggling
 * - Pull-to-refresh coordination
 *
 * Pattern: shared/BasePortfolioViewModel.kt
 */
/**
 * Shared portfolio state management.
 * Used by: HomeScreen, WalletsScreen, PortfolioWidget
 *
 * KOIN DEPENDENCY INJECTION
 *
 * Pattern: shared/BasePortfolioViewModel.kt
 */
open class BasePortfolioViewModel(
    private val observePortfolioUseCase: ObservePortfolioUseCase,
    private val refreshAssetsUseCase: RefreshAssetsUseCase,
    private val toggleAssetVisibilityUseCase: ToggleAssetVisibilityUseCase,
    private val observeCurrencyUseCase: ObserveCurrencyPreferenceUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    // UI State: Portfolio data
    private val _portfolioState = MutableStateFlow<LoadingState<PortfolioState>>(LoadingState.Loading)
    val portfolioState: StateFlow<LoadingState<PortfolioState>> = _portfolioState.asStateFlow()

    // UI State: Refresh indicator
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // UI State: Network status (for offline banner)
    val networkStatus: StateFlow<NetworkStatus> = networkMonitor.isConnected
        .combine(networkMonitor.connectionType) { connected, type ->
            NetworkStatus(
                isConnected = connected,
                connectionType = type,
                isMetered = type == ConnectionType.CELLULAR
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkStatus(
                isConnected = true,
                connectionType = ConnectionType.WIFI,
                isMetered = false
            )
        )

    // UI State: Selected currency for formatting
    val selectedCurrency: StateFlow<String> = observeCurrencyUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = "USD"
        )

    init {
        observePortfolio()
    }

    private fun observePortfolio() {
        viewModelScope.launch {
            observePortfolioUseCase()
                .distinctUntilChanged()
                .collect { state ->
                    _portfolioState.value = state
                }
        }
    }

    fun refresh() {
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true

            when (val result = refreshAssetsUseCase()) {
                is LoadingState.Success -> {
                    // Portfolio state updates automatically via observePortfolio()
                }
                is LoadingState.Error -> {
                    _portfolioState.value = LoadingState.Error(
                        result.throwable,
                        "Refresh failed. Showing cached data."
                    )
                }
                else -> {}
            }

            _isRefreshing.value = false
        }
    }

    fun toggleAssetVisibility(assetId: String, isHidden: Boolean) {
        viewModelScope.launch {
            toggleAssetVisibilityUseCase(assetId, isHidden)
                .onFailure { e ->
                    _portfolioState.value = LoadingState.Error(
                        e,
                        "Failed to update asset visibility"
                    )
                }
        }
    }

    fun formatCurrency(valueUsd: Double): String {
        val currency = selectedCurrency.value
        return when (currency) {
            "USD" -> "$${"%,.2f".format(valueUsd)}"
            "EUR" -> "€${"%,.2f".format(valueUsd * 0.92)}"
            "GBP" -> "£${"%,.2f".format(valueUsd * 0.79)}"
            else -> "$${"%,.2f".format(valueUsd)}"
        }
    }

    fun formatChange(changePercent: Double): Pair<Color, String> {
        val color = when {
            changePercent > 0 -> Color(0xFF4ECDC4)
            changePercent < 0 -> Color(0xFFFF6B6B)
            else -> Color.Gray
        }

        val sign = if (changePercent > 0) "+" else ""
        val formatted = "$sign${"%.2f".format(changePercent)}%"

        return color to formatted
    }
}

data class NetworkStatus(
    val isConnected: Boolean,
    val connectionType: ConnectionType,
    val isMetered: Boolean
)