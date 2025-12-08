package com.decagon.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonSettingsViewModel(
    private val repository: DecagonWalletRepository,
    private val mnemonic: DecagonMnemonic
) : ViewModel() {

    private val _mnemonicState = MutableStateFlow<DecagonLoadingState<String>>(
        DecagonLoadingState.Idle
    )
    val mnemonicState: StateFlow<DecagonLoadingState<String>> = _mnemonicState.asStateFlow()

    fun loadMnemonic(walletId: String) {
        Timber.d("Loading mnemonic for wallet: $walletId")
        viewModelScope.launch {
            _mnemonicState.value = DecagonLoadingState.Loading

            repository.decryptSeed(walletId)
                .onSuccess { seed ->
                    try {
                        // Derive mnemonic from seed (this is a simplification)
                        // In production, store encrypted mnemonic separately
                        val phrase = "Recovery phrase cannot be derived from seed"
                        _mnemonicState.value = DecagonLoadingState.Success(phrase)
                        Timber.w("Mnemonic derivation not implemented - seed is one-way")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to process seed")
                        _mnemonicState.value = DecagonLoadingState.Error(
                            e,
                            "Failed to retrieve recovery phrase"
                        )
                    }
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to decrypt seed")
                    _mnemonicState.value = DecagonLoadingState.Error(
                        error,
                        error.message ?: "Failed to access wallet"
                    )
                }
        }
    }

    fun removeWallet(walletId: String) {
        Timber.i("Removing wallet: $walletId")
        viewModelScope.launch {
            repository.deleteWallet(walletId)
        }
    }
}