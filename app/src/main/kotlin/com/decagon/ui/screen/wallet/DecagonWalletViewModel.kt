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
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonWalletViewModel(
    private val repository: DecagonWalletRepository,
    private val rpcClient: SolanaRpcClient
) : ViewModel() {

    init {
        Timber.d("DecagonWalletViewModel initialized.")
    }

    val walletState: StateFlow<DecagonLoadingState<DecagonWallet>> =
        repository.getActiveWallet()
            .map { wallet ->
                if (wallet != null) {
                    Timber.d("Active wallet loaded: ${wallet.id}")

                    // Fetch real balance
                    val balanceResult = rpcClient.getBalance(wallet.address)
                    val balanceSol = balanceResult.getOrNull()?.let { it / 1_000_000_000.0 } ?: 0.0

                    Timber.i("Wallet balance: $balanceSol SOL")

                    DecagonLoadingState.Success(
                        wallet.copy(balance = balanceSol)
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

    val allWallets: StateFlow<List<DecagonWallet>> =
        repository.getAllWallets()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun switchWallet(walletId: String) {
        Timber.i("Switching to wallet: $walletId")
        viewModelScope.launch {
            repository.setActiveWallet(walletId)
        }
    }
}