package com.octane.wallet.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType
import com.octane.wallet.domain.usecases.transaction.ObserveTransactionHistoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Activity screen ViewModel.
 * Displays transaction history with pagination, filtering, search.
 *
 * RESPONSIBILITY:
 * - Paginated transaction list (infinite scroll)
 * - Transaction filtering (type, status, date range)
 * - Search by hash/address
 * - Transaction details sheet
 * - Export transactions (CSV)
 *
 * Pattern: features/activity/ActivityViewModel.kt
 */
class ActivityViewModel(
    private val baseTransaction: BaseTransactionViewModel,
    private val observeTransactionHistoryUseCase: ObserveTransactionHistoryUseCase
) : ViewModel() {

    // Delegate: Recent transactions
    val recentTransactions = baseTransaction.recentTransactions

    // Delegate: Pending count
    val pendingCount = baseTransaction.pendingCount

    // UI State: Filters and search
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    // Filtered transactions
    val filteredTransactions: StateFlow<LoadingState<List<Transaction>>> = combine(
        recentTransactions,
        _uiState
    ) { txState, uiState ->
        when (txState) {
            is LoadingState.Success -> {
                val filtered = txState.data.filter { tx ->
                    matchesFilters(tx, uiState)
                }
                LoadingState.Success(filtered)
            }

            is LoadingState.Loading -> LoadingState.Loading
            is LoadingState.Error -> txState
            else -> LoadingState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoadingState.Loading
    )

    /**
     * Show transaction details sheet.
     */
    fun showTransactionDetails(transaction: Transaction) {
        _uiState.update {
            it.copy(
                selectedTransaction = transaction,
                showDetailsSheet = true
            )
        }
    }

    fun hideTransactionDetails() {
        _uiState.update {
            it.copy(
                selectedTransaction = null,
                showDetailsSheet = false
            )
        }
    }

    /**
     * Update search query (debounced).
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /**
     * Toggle transaction type filter.
     */
    fun toggleTypeFilter(type: TransactionType) {
        _uiState.update {
            val currentFilters = it.typeFilters.toMutableSet()
            if (currentFilters.contains(type)) {
                currentFilters.remove(type)
            } else {
                currentFilters.add(type)
            }
            it.copy(typeFilters = currentFilters)
        }
    }

    /**
     * Toggle status filter.
     */
    fun toggleStatusFilter(status: TransactionStatus) {
        _uiState.update {
            val currentFilters = it.statusFilters.toMutableSet()
            if (currentFilters.contains(status)) {
                currentFilters.remove(status)
            } else {
                currentFilters.add(status)
            }
            it.copy(statusFilters = currentFilters)
        }
    }

    /**
     * Set date range filter.
     */
    fun setDateRange(start: Long?, end: Long?) {
        _uiState.update {
            it.copy(
                dateRangeStart = start,
                dateRangeEnd = end
            )
        }
    }

    /**
     * Clear all filters.
     */
    fun clearFilters() {
        _uiState.update {
            ActivityUiState()
        }
    }

    /**
     * Check if transaction matches current filters.
     */
    private fun matchesFilters(tx: Transaction, state: ActivityUiState): Boolean {
        // Search query
        if (state.searchQuery.isNotBlank()) {
            val query = state.searchQuery.lowercase()
            val matchesHash = tx.txHash.lowercase().contains(query)
            val matchesAddress = tx.toAddress?.lowercase()?.contains(query) == true
            if (!matchesHash && !matchesAddress) return false
        }

        // Type filter
        if (state.typeFilters.isNotEmpty() && !state.typeFilters.contains(tx.type)) {
            return false
        }

        // Status filter
        if (state.statusFilters.isNotEmpty() && !state.statusFilters.contains(tx.status)) {
            return false
        }

        // Date range
        if (state.dateRangeStart != null && tx.timestamp < state.dateRangeStart) {
            return false
        }
        if (state.dateRangeEnd != null && tx.timestamp > state.dateRangeEnd) {
            return false
        }

        return true
    }

    /**
     * Export transactions as CSV.
     */
    fun exportTransactions() {
        viewModelScope.launch {
            val transactions = (filteredTransactions.value as? LoadingState.Success)?.data
            if (transactions != null) {
                val csv = generateCSV(transactions)
                // TODO: Save to file and share
            }
        }
    }

    /**
     * Generate CSV from transactions.
     */
    private fun generateCSV(transactions: List<Transaction>): String {
        val header = "Date,Type,Status,From,To,Amount,Token,Fee,Hash\n"
        val rows = transactions.joinToString("\n") { tx ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
                .format(java.util.Date(tx.timestamp))

            "${date}," +
                    "${tx.type}," +
                    "${tx.status}," +
                    "${tx.fromAddress}," +
                    "${tx.toAddress ?: ""}," +
                    "${tx.amount}," +
                    "${tx.tokenSymbol}," +
                    "${tx.fee}," +
                    "${tx.txHash}"
        }
        return header + rows
    }

    /**
     * Format transaction for display (delegates to base).
     */
    fun formatTransactionType(type: TransactionType): String {
        return baseTransaction.formatTransactionType(type)
    }

    fun getTransactionIcon(type: TransactionType): String {
        return baseTransaction.getTransactionIcon(type)
    }

    fun getStatusColor(status: TransactionStatus): Color {
        return baseTransaction.getStatusColor(status)
    }
}

/**
 * Activity UI state (filters, search, sheets).
 */
data class ActivityUiState(
    val searchQuery: String = "",
    val typeFilters: Set<TransactionType> = emptySet(),
    val isLoadingMore: Boolean = false,
    val statusFilters: Set<TransactionStatus> = emptySet(),
    val dateRangeStart: Long? = null,
    val dateRangeEnd: Long? = null,
    val selectedTransaction: Transaction? = null,
    val showDetailsSheet: Boolean = false
)