package com.decagon.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber // Import added

class DecagonWalletViewModel(
    repository: DecagonWalletRepository
) : ViewModel() {

    init {
        Timber.d("DecagonWalletViewModel initialized.") // Log added
    }

    val walletState: StateFlow<DecagonLoadingState<DecagonWallet>> =
        repository.getActiveWallet()
            .map { wallet ->
                if (wallet != null) {
                    Timber.d("Active wallet loaded: ${wallet.id}") // Log added
                    DecagonLoadingState.Success(wallet)
                } else {
                    Timber.e("No active wallet found in repository.") // Log added
                    DecagonLoadingState.Error(
                        IllegalStateException("No active wallet"),
                        "No wallet found"
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DecagonLoadingState.Loading
            )
}