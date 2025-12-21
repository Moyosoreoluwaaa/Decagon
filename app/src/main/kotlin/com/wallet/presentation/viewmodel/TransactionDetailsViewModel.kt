package com.octane.wallet.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.usecases.transaction.GetTransactionByHashUseCase
import com.wallet.presentation.navigation.AppRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/**
 * Transaction details ViewModel.
 * Displays full transaction info with real-time status updates.
 */
class TransactionDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    getTransactionByHashUseCase: GetTransactionByHashUseCase
) : ViewModel() {

    private val route = savedStateHandle.toRoute<AppRoute.TransactionDetails>()
    private val txHash = route.txHash

    val transaction: StateFlow<Transaction?> = getTransactionByHashUseCase(txHash)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Get Solscan explorer URL for transaction.
     */
    fun getExplorerUrl(): String {
        return "https://solscan.io/tx/$txHash"
    }

    /**
     * Format transaction hash for display (truncated).
     */
    fun formatTxHash(): String {
        return txHash.take(8) + "..." + txHash.takeLast(8)
    }
}