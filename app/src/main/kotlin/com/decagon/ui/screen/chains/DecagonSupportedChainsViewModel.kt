package com.decagon.ui.screen.chains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DecagonSupportedChainsViewModel(
    private val walletRepository: DecagonWalletRepository
) : ViewModel() {
    
    fun getWallet(walletId: String): StateFlow<DecagonLoadingState<DecagonWallet>> {
        return walletRepository.getWalletById(walletId)
            .map { wallet ->
                if (wallet != null) {
                    DecagonLoadingState.Success(wallet)
                } else {
                    DecagonLoadingState.Error(
                        IllegalArgumentException("Wallet not found"),
                        "Wallet not found"
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = DecagonLoadingState.Loading
            )
    }
    
    fun switchChain(walletId: String, chainId: String) {
        viewModelScope.launch {
            walletRepository.setActiveChain(walletId, chainId)
        }
    }
}