package com.decagon.ui.screen.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.DecagonLoadingState
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber // Import added

// DecagonWalletViewModel.kt
class DecagonWalletViewModel(
    private val repository: DecagonWalletRepository,
    private val rpcClient: SolanaRpcClient // ← ADD THIS
) : ViewModel() {

    init {
        Timber.d("DecagonWalletViewModel initialized.")
    }

    val walletState: StateFlow<DecagonLoadingState<DecagonWallet>> =
        repository.getActiveWallet()
            .map { wallet ->
                if (wallet != null) {
                    Timber.d("Active wallet loaded: ${wallet.id}")

                    // ✅ FETCH REAL BALANCE
                    val balanceResult = rpcClient.getBalance(wallet.address)
                    val balanceSol = balanceResult.getOrNull()?.let { it / 1_000_000_000.0 } ?: 0.0

                    Timber.i("Wallet balance: $balanceSol SOL")

                    DecagonLoadingState.Success(
                        wallet.copy(balance = balanceSol) // Update balance
                    )
                } else {
                    Timber.e("No active wallet found in repository.")
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