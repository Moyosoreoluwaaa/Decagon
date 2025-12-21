package com.wallet.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType
import com.wallet.domain.repository.WalletRepository
import com.wallet.domain.usecases.transaction.MonitorPendingTransactionsUseCase
import com.wallet.domain.usecases.transaction.ObserveTransactionHistoryUseCase
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class BaseTransactionViewModel(
    private val observeTransactionHistoryUseCase: ObserveTransactionHistoryUseCase,
    private val monitorPendingTransactionsUseCase: MonitorPendingTransactionsUseCase,
    private val walletRepository: WalletRepository
) : ViewModel() {

    // UI State: Recent transactions (last 50 for home screen)
    val recentTransactions: StateFlow<LoadingState<List<Transaction>>> =
        observeTransactionHistoryUseCase(limit = 50)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = LoadingState.Loading
            )

    // UI State: Pending transactions count (for badge)
    val pendingCount: StateFlow<Int> = recentTransactions
        .map { state ->
            when (state) {
                is LoadingState.Success -> state.data.count {
                    it.status == TransactionStatus.PENDING
                }

                else -> 0
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        startPendingTransactionMonitor()
    }

    /**
     * Monitor pending transactions every 5 seconds.
     * Updates status when confirmed/failed.
     */
    private fun startPendingTransactionMonitor() {
        viewModelScope.launch {
            while (isActive) {
                monitorPendingTransactionsUseCase()
                delay(5000) // Poll every 5 seconds
            }
        }
    }

    /**
     * Get transaction by hash (for deep links, notifications).
     * @param txHash Solana transaction signature
     * @return Transaction or null if not found
     */
    suspend fun getTransactionByHash(txHash: String): Transaction? {
        return (recentTransactions.value as? LoadingState.Success)
            ?.data
            ?.find { it.txHash == txHash }
    }

    /**
     * Format transaction type for display.
     * @param type Transaction type enum
     * @return User-friendly string ("Sent", "Received", "Swapped")
     */
    fun formatTransactionType(type: TransactionType): String {
        return when (type) {
            TransactionType.SEND -> "Sent"
            TransactionType.RECEIVE -> "Received"
            TransactionType.SWAP -> "Swapped"
            TransactionType.STAKE -> "Staked"
            TransactionType.UNSTAKE -> "Unstaked"
            TransactionType.NFT_MINT -> "Minted"
            TransactionType.NFT_TRANSFER -> "Transferred NFT"
            else -> "Transaction"
        }
    }

    /**
     * Get icon for transaction type.
     * @param type Transaction type enum
     * @return Icon resource or emoji
     */
    fun getTransactionIcon(type: TransactionType): String {
        return when (type) {
            TransactionType.SEND -> "\u2197\uFE0F"      // ↗️
            TransactionType.RECEIVE -> "\u2198\uFE0F"   // ↘️
            TransactionType.SWAP -> "\uD83D\uDD04"      // 🔄
            TransactionType.STAKE -> "\uD83D\uDD12"     // 🔒
            TransactionType.UNSTAKE -> "\uD83D\uDD13"   // 🔓
            TransactionType.NFT_MINT -> "\uD83C\uDFA8"  // 🎨
            TransactionType.NFT_TRANSFER -> "\uD83D\uDDBC\uFE0F" // 🖼️
            else -> "\u26A1"                            // ⚡
        }
    }

    /**
     * Get status badge color.
     * @param status Transaction status
     * @return Color for status badge
     */
    fun getStatusColor(status: TransactionStatus): Color {
        return when (status) {
            TransactionStatus.CONFIRMED -> Color(0xFF4ECDC4) // Teal
            TransactionStatus.PENDING -> Color(0xFFF7DC6F) // Yellow
            TransactionStatus.FAILED -> Color(0xFFFF6B6B) // Red
        }
    }
}
