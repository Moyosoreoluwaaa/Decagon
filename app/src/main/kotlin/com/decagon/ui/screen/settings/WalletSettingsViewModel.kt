package com.decagon.ui.screen.settings

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.network.RpcClientFactory
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.repository.DecagonSettingsRepository
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.util.TransactionDiagnostic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for wallet-specific settings.
 * NO setActivity() - activity passed as function parameter.
 * 
 * Responsibilities:
 * - Reveal mnemonic/private key (requires biometric)
 * - Edit wallet name
 * - Remove wallet (requires biometric)
 * - Fix stuck transactions
 */
class WalletSettingsViewModel(
    private val settingsRepository: DecagonSettingsRepository,
    private val walletRepository: DecagonWalletRepository,
    private val transactionRepository: DecagonTransactionRepository,
    private val rpcFactory: RpcClientFactory
) : ViewModel() {

    private val _mnemonicState = MutableStateFlow<DecagonLoadingState<String>>(
        DecagonLoadingState.Idle
    )
    val mnemonicState: StateFlow<DecagonLoadingState<String>> = _mnemonicState.asStateFlow()

    private val _privateKeyState = MutableStateFlow<DecagonLoadingState<String>>(
        DecagonLoadingState.Idle
    )
    val privateKeyState: StateFlow<DecagonLoadingState<String>> = _privateKeyState.asStateFlow()

    private val _editNameState = MutableStateFlow<DecagonLoadingState<Unit>>(
        DecagonLoadingState.Idle
    )
    val editNameState: StateFlow<DecagonLoadingState<Unit>> = _editNameState.asStateFlow()

    init {
        Timber.d("WalletSettingsViewModel initialized")
    }

    /**
     * Load mnemonic with biometric authentication.
     * Activity passed as parameter - no leak.
     */
    fun loadMnemonic(walletId: String, activity: FragmentActivity) {
        Timber.d("Loading mnemonic for wallet: $walletId")
        viewModelScope.launch {
            _mnemonicState.value = DecagonLoadingState.Loading

            settingsRepository.revealRecoveryPhrase(walletId, activity)
                .onSuccess { mnemonic ->
                    Timber.i("Mnemonic revealed successfully")
                    _mnemonicState.value = DecagonLoadingState.Success(mnemonic)
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to reveal mnemonic")
                    _mnemonicState.value = DecagonLoadingState.Error(
                        error,
                        error.message ?: "Failed to retrieve recovery phrase"
                    )
                }
        }
    }

    /**
     * Load private key with biometric authentication.
     * Activity passed as parameter - no leak.
     */
    fun loadPrivateKey(walletId: String, activity: FragmentActivity) {
        Timber.d("Loading private key for wallet: $walletId")
        viewModelScope.launch {
            _privateKeyState.value = DecagonLoadingState.Loading

            settingsRepository.revealPrivateKey(walletId, activity)
                .onSuccess { privateKey ->
                    Timber.i("Private key revealed successfully")
                    _privateKeyState.value = DecagonLoadingState.Success(privateKey)
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to reveal private key")
                    _privateKeyState.value = DecagonLoadingState.Error(
                        error,
                        error.message ?: "Failed to retrieve private key"
                    )
                }
        }
    }

    /**
     * Update wallet name.
     */
    fun updateWalletName(walletId: String, newName: String) {
        Timber.d("Updating wallet name: $walletId -> $newName")
        viewModelScope.launch {
            _editNameState.value = DecagonLoadingState.Loading

            settingsRepository.updateWalletName(walletId, newName)
                .onSuccess {
                    Timber.i("Wallet name updated successfully")
                    _editNameState.value = DecagonLoadingState.Success(Unit)
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to update wallet name")
                    _editNameState.value = DecagonLoadingState.Error(
                        error,
                        error.message ?: "Failed to update name"
                    )
                }
        }
    }

    /**
     * Remove wallet with biometric authentication.
     * Activity passed as parameter - no leak.
     */
    fun removeWallet(walletId: String, activity: FragmentActivity, onSuccess: () -> Unit) {
        Timber.i("Removing wallet: $walletId")
        viewModelScope.launch {
            settingsRepository.removeWallet(walletId, activity)
                .onSuccess {
                    Timber.i("Wallet removed successfully")
                    onSuccess()
                }
                .onFailure { error ->
                    Timber.e(error, "Failed to remove wallet")
                }
        }
    }

    /**
     * Fix stuck transactions using network-aware diagnostic.
     */
    fun fixStuckTransactions(walletAddress: String) {
        Timber.i("Starting stuck transaction fix for: ${walletAddress.take(8)}...")
        viewModelScope.launch {
            val diagnostic = TransactionDiagnostic(
                transactionRepository = transactionRepository,
                rpcFactory = rpcFactory,
                walletRepository = walletRepository
            )
            val fixed = diagnostic.diagnoseAndFixPending(walletAddress)
            Timber.i("✅ Fixed $fixed stuck transactions")
        }
    }

    fun resetMnemonicState() {
        _mnemonicState.value = DecagonLoadingState.Idle
    }

    fun resetPrivateKeyState() {
        _privateKeyState.value = DecagonLoadingState.Idle
    }

    fun resetEditNameState() {
        _editNameState.value = DecagonLoadingState.Idle
    }
}