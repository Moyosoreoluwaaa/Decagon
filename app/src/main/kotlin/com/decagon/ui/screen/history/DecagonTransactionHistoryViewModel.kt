package com.decagon.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.*
import timber.log.Timber

class DecagonTransactionHistoryViewModel(
    private val transactionRepository: DecagonTransactionRepository,
    private val walletRepository: DecagonWalletRepository
) : ViewModel() {

    init {
        Timber.d("DecagonTransactionHistoryViewModel initialized")
    }

    // ✅ Transaction history for active wallet
    val transactions: StateFlow<DecagonLoadingState<List<DecagonTransaction>>> =
        walletRepository.getActiveWallet()
            .filterNotNull()
            .flatMapLatest { wallet ->
                Timber.d("Starting to load transactions for wallet: ${wallet.address.take(8)}...")
                transactionRepository.getTransactionHistory(wallet.address)
                    .map { txList ->
                        // Transform the successful data list into a Success state
                        Timber.i("Loaded ${txList.size} transactions for wallet: ${wallet.address.take(8)}...")
                        DecagonLoadingState.Success(txList) as DecagonLoadingState<List<DecagonTransaction>>
                    }
                    .catch { error ->
                        // Transform the error into an Error state
                        Timber.e(error, "Failed to load transactions for wallet: ${wallet.address.take(8)}...")
                        emit(DecagonLoadingState.Error(error, error.message ?: "Failed to load"))
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DecagonLoadingState.Loading
            )

    // ✅ Grouped transactions by date
    val groupedTransactions: StateFlow<DecagonLoadingState<Map<String, List<DecagonTransaction>>>> =
        transactions.map { state ->
            when (state) {
                is DecagonLoadingState.Success -> {
                    val grouped = state.data.groupBy { tx ->
                        formatDateGroup(tx.timestamp)
                    }
                    DecagonLoadingState.Success(grouped)
                }
                is DecagonLoadingState.Loading -> DecagonLoadingState.Loading
                // IMPORTANT: When mapping a DecagonLoadingState<T>, the Error/Idle states must be propagated.
                is DecagonLoadingState.Error -> DecagonLoadingState.Error(state.throwable, state.message)
                is DecagonLoadingState.Idle -> DecagonLoadingState.Idle
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DecagonLoadingState.Loading
        )

    private fun formatDateGroup(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 86_400_000 -> "Today" // < 24 hours
            diff < 172_800_000 -> "Yesterday" // < 48 hours
            diff < 604_800_000 -> "This Week" // < 7 days
            else -> {
                val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US)
                    .format(java.util.Date(timestamp))
                date
            }
        }
    }
}