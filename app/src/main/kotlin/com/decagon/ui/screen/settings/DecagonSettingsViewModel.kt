package com.decagon.ui.screen.settings

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.DecagonLoadingState
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.repository.DecagonSettingsRepository
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.util.TransactionDiagnostic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonSettingsViewModel(
    private val settingsRepository: DecagonSettingsRepository,
    private val walletRepository: DecagonWalletRepository,
    private val transactionRepository: DecagonTransactionRepository, // ✅ Inject
    private val rpcClient: SolanaRpcClient // ✅ Inject
) : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private var currentActivity: FragmentActivity? = null

    fun setActivity(activity: FragmentActivity?) {
        currentActivity = activity
        Timber.d("Settings activity: ${if (activity != null) "Attached" else "Detached"}")
    }

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
        Timber.d("DecagonSettingsViewModel initialized")
    }

    fun loadMnemonic(walletId: String) {
        val activity = currentActivity ?: run {
            Timber.e("Cannot load mnemonic: Activity is null")
            _mnemonicState.value = DecagonLoadingState.Error(
                IllegalStateException("Activity required"),
                "Cannot access biometric authentication"
            )
            return
        }

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

    fun loadPrivateKey(walletId: String) {
        val activity = currentActivity ?: run {
            Timber.e("Cannot load private key: Activity is null")
            _privateKeyState.value = DecagonLoadingState.Error(
                IllegalStateException("Activity required"),
                "Cannot access biometric authentication"
            )
            return
        }

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

    fun removeWallet(walletId: String, onSuccess: () -> Unit) {
        val activity = currentActivity ?: run {
            Timber.e("Cannot remove wallet: Activity is null")
            return
        }

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

    fun resetMnemonicState() {
        _mnemonicState.value = DecagonLoadingState.Idle
    }

    fun resetPrivateKeyState() {
        _privateKeyState.value = DecagonLoadingState.Idle
    }

    fun resetEditNameState() {
        _editNameState.value = DecagonLoadingState.Idle
    }

    fun fixStuckTransactions(walletAddress: String) {
        viewModelScope.launch {
            val diagnostic = TransactionDiagnostic(transactionRepository, rpcClient)
            val fixed = diagnostic.diagnoseAndFixPending(walletAddress)
            Timber.i("Fixed $fixed stuck transactions")
        }
    }
}