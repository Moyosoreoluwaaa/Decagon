package com.wallet.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Asset
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.Wallet
import com.octane.wallet.domain.usecases.asset.PortfolioState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
 * Home screen orchestrator ViewModel.
 * Delegates to Base ViewModels for shared state management.
 */
class HomeViewModel(
    private val basePortfolio: BasePortfolioViewModel,
    private val baseWallet: BaseWalletViewModel,
    private val baseTransaction: BaseTransactionViewModel
) : ViewModel() {

    // ✅ NEW: Track pending action when user clicks quick action without wallet
    private val _pendingAction = MutableStateFlow<PendingAction?>(null)
    private val _showWalletRequired = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    val showWalletRequired: SharedFlow<Unit> = _showWalletRequired.asSharedFlow()

    // ==================== Delegated State ====================
    
    /**
     * Portfolio state (total value, assets, P&L).
     * Observes the active wallet's portfolio.
     */
    val portfolioState: StateFlow<LoadingState<PortfolioState>> = basePortfolio.portfolioState
    
    /**
     * Active wallet details.
     */
    val activeWallet: StateFlow<Wallet?> = baseWallet.activeWallet
    
    /**
     * Recent transactions (last 10 for home screen preview).
     */
    val recentTransactions: StateFlow<LoadingState<List<Transaction>>> = baseTransaction.recentTransactions
    
    /**
     * Pending transaction count (for badge).
     */
    val pendingCount: StateFlow<Int> = baseTransaction.pendingCount
    
    /**
     * Refresh state (pull-to-refresh indicator).
     */
    val isRefreshing: StateFlow<Boolean> = basePortfolio.isRefreshing
    
    /**
     * Network status (for offline banner).
     */
    val networkStatus: StateFlow<NetworkStatus> = basePortfolio.networkStatus
    
    // ==================== One-Time Events ====================
    
    private val _events = MutableSharedFlow<HomeEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    // ✅ Monitor active wallet changes and resume pending actions
    init {
        viewModelScope.launch {
            activeWallet.collect { wallet ->
                if (wallet != null) {
                    _pendingAction.value?.let { action ->
                        resumePendingAction(action)
                        _pendingAction.value = null // Clear after resuming
                    }
                }
            }
        }
    }
    
    // ==================== Actions ====================
    
    /**
     * Refresh portfolio data (pull-to-refresh).
     */
    fun onRefresh() {
        basePortfolio.refresh()
    }
    
    /**
     * Toggle asset visibility (hide/show from home screen).
     */
    fun onToggleAssetVisibility(assetId: String, isHidden: Boolean) {
        basePortfolio.toggleAssetVisibility(assetId, isHidden)
    }
    
    /**
     * Navigate to wallet switcher.
     */
    fun onWalletClick() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToWallets)
        }
    }
    
    /**
     * Quick action: Send (opens send screen with token pre-selected).
     */

    // ✅ Updated quick actions - save intent if no wallet
    fun onQuickSend(tokenSymbol: String) {
        if (activeWallet.value == null) {
            _pendingAction.value = PendingAction(ActionType.SEND, tokenSymbol)
            viewModelScope.launch {
                _showWalletRequired.emit(Unit)
            }
        } else {
            viewModelScope.launch {
                _events.emit(HomeEvent.NavigateToSend(tokenSymbol))
            }
        }
    }
    
    /**
     * Quick action: Receive (opens receive screen).
     */
    fun onQuickReceive() {
        if (activeWallet.value == null) {
            _pendingAction.value = PendingAction(ActionType.RECEIVE)
            viewModelScope.launch {
                _showWalletRequired.emit(Unit)
            }
        } else {
            viewModelScope.launch {
                _events.emit(HomeEvent.NavigateToReceive)
            }
        }
    }
    
    /**
     * Quick action: Swap (opens swap screen).
     */
    fun onQuickSwap() {
        if (activeWallet.value == null) {
            _pendingAction.value = PendingAction(ActionType.SWAP)
            viewModelScope.launch {
                _showWalletRequired.emit(Unit)
            }
        } else {
            viewModelScope.launch {
                _events.emit(HomeEvent.NavigateToSwap)
            }
        }
    }


    fun onQuickBuy() {
        if (activeWallet.value == null) {
            _pendingAction.value = PendingAction(ActionType.BUY)
            viewModelScope.launch {
                _showWalletRequired.emit(Unit)
            }
        } else {
            viewModelScope.launch {
                _events.emit(HomeEvent.NavigateToBuy)
            }
        }
    }

    // ✅ Resume pending action after wallet creation
    private suspend fun resumePendingAction(action: PendingAction) {
        when (action.type) {
            ActionType.SEND -> _events.emit(
                HomeEvent.NavigateToSend(action.data ?: "SOL")
            )
            ActionType.RECEIVE -> _events.emit(HomeEvent.NavigateToReceive)
            ActionType.SWAP -> _events.emit(HomeEvent.NavigateToSwap)
            ActionType.BUY -> _events.emit(HomeEvent.NavigateToBuy)
        }
    }


    /**
     * Quick action: Manage tokens.
     */
    fun onManageTokens() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToManage)
        }
    }
    
    /**
     * Navigate to token details.
     */
    fun onAssetClick(asset: Asset) {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToDetails(asset.id, asset.symbol))
        }
    }
    
    /**
     * Navigate to transaction details.
     */
    fun onTransactionClick(transaction: Transaction) {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToTransactionDetails(transaction.txHash))
        }
    }
    
    // ==================== Formatting Helpers ====================
    
    /**
     * Format currency value based on user preference.
     */
    fun formatCurrency(valueUsd: Double): String {
        return basePortfolio.formatCurrency(valueUsd)
    }
    
    /**
     * Format change percentage with color.
     */
    fun formatChange(changePercent: Double): Pair<Color, String> {
        return basePortfolio.formatChange(changePercent)
    }
}

// ✅ NEW: Pending action model
data class PendingAction(
    val type: ActionType,
    val data: String? = null
)

enum class ActionType {
    SEND, RECEIVE, SWAP, BUY
}

/**
 * One-time navigation events from Home screen.
 */

// ✅ Update events
sealed interface HomeEvent {
    data object NavigateToWallets : HomeEvent
    data class NavigateToSend(val tokenSymbol: String) : HomeEvent
    data object NavigateToReceive : HomeEvent
    data object NavigateToSwap : HomeEvent
    data object NavigateToBuy : HomeEvent // NEW
    data object NavigateToManage : HomeEvent
    data class NavigateToDetails(val assetId: String, val symbol: String) : HomeEvent
    data class NavigateToTransactionDetails(val txHash: String) : HomeEvent
}