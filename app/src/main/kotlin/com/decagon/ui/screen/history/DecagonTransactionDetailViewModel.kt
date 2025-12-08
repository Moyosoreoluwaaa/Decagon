package com.decagon.ui.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.repository.DecagonTransactionRepository
import kotlinx.coroutines.flow.*
import timber.log.Timber

class DecagonTransactionDetailViewModel(
    private val transactionRepository: DecagonTransactionRepository
) : ViewModel() {

    init {
        Timber.d("DecagonTransactionDetailViewModel initialized")
    }

    // ✅ Get transaction by ID
    fun getTransaction(txId: String): StateFlow<DecagonLoadingState<DecagonTransaction>> {
        Timber.d("Loading transaction: $txId")
        
        return transactionRepository.getTransactionById(txId)
            .map { tx ->
                if (tx != null) {
                    Timber.i("Transaction loaded: $txId")
                    DecagonLoadingState.Success(tx)
                } else {
                    Timber.w("Transaction not found: $txId")
                    DecagonLoadingState.Error(
                        IllegalArgumentException("Transaction not found"),
                        "Transaction not found"
                    )
                }
            }
            .catch { error ->
                Timber.e(error, "Failed to load transaction: $txId")
                emit(DecagonLoadingState.Error(error, error.message ?: "Failed to load"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DecagonLoadingState.Loading
            )
    }
}