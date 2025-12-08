package com.decagon.ui.screen.onboarding

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.usecase.DecagonCreateWalletUseCase
import com.decagon.domain.usecase.DecagonImportWalletUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonOnboardingViewModel(
    private val createWalletUseCase: DecagonCreateWalletUseCase,
    private val importWalletUseCase: DecagonImportWalletUseCase,
    private val biometricAuthenticator: DecagonBiometricAuthenticator
) : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private var currentActivity: FragmentActivity? = null

    fun setActivity(activity: FragmentActivity?) {
        currentActivity = activity
        biometricAuthenticator.setActivity(activity)
        Timber.d("Activity set for ViewModel: ${if (activity != null) "Attached" else "Detached"}")
    }

    private val _uiState = MutableStateFlow<DecagonLoadingState<OnboardingState>>(
        DecagonLoadingState.Idle
    )
    val uiState: StateFlow<DecagonLoadingState<OnboardingState>> = _uiState.asStateFlow()

    private val _importState = MutableStateFlow<DecagonLoadingState<OnboardingState>>(
        DecagonLoadingState.Idle
    )
    val importState: StateFlow<DecagonLoadingState<OnboardingState>> = _importState.asStateFlow()

    init {
        Timber.i("DecagonOnboardingViewModel initialized.")
        checkBiometricStatus()
    }

    private fun checkBiometricStatus() {
        Timber.d("Checking biometric status.")
        val status = biometricAuthenticator.checkBiometricStatus()
        if (!status.isAvailable) {
            Timber.e("Biometric not available: ${status.getUserMessage()}")
            _uiState.value = DecagonLoadingState.Error(
                IllegalStateException("Biometric unavailable"),
                status.getUserMessage()
            )
        } else {
            Timber.i("Biometric is available and ready for use.")
        }
    }

    fun createWallet(name: String) {
        val activity = currentActivity ?: run {
            Timber.e("Cannot create wallet: Activity is null. Stopping.")
            _uiState.value = DecagonLoadingState.Error(
                IllegalStateException("Activity required"),
                "Cannot access biometric authentication"
            )
            return
        }

        Timber.i("Attempting to create new wallet with name: $name")
        viewModelScope.launch {
            _uiState.value = DecagonLoadingState.Loading
            Timber.d("UI state set to Loading for wallet creation.")

            try {
                val result = createWalletUseCase(name, activity).getOrThrow()
                Timber.i("Wallet created successfully: ID=${result.wallet.id}, Address=${result.wallet.address.take(8)}...")

                _uiState.value = DecagonLoadingState.Success(
                    OnboardingState.WalletCreated(
                        walletId = result.wallet.id,
                        walletName = result.wallet.name,
                        mnemonic = result.mnemonic,
                        address = result.wallet.address
                    )
                )
                Timber.d("UI state set to Success (WalletCreated).")
            } catch (e: Exception) {
                Timber.e(e, "FATAL: Error creating wallet: $name")
                _uiState.value = DecagonLoadingState.Error(e, "Failed to create wallet: ${e.message}")
                Timber.d("UI state set to Error.")
            }
        }
    }

    fun importWallet(name: String, mnemonic: String) {
        val activity = currentActivity ?: run {
            Timber.e("Cannot import wallet: Activity is null. Stopping.")
            _importState.value = DecagonLoadingState.Error(
                IllegalStateException("Activity required"),
                "Cannot access biometric authentication"
            )
            return
        }

        Timber.i("Attempting to import wallet: $name")
        viewModelScope.launch {
            _importState.value = DecagonLoadingState.Loading
            Timber.d("Import state set to Loading.")

            importWalletUseCase(
                name = name,
                phrase = mnemonic,
                activity = activity
            )
                .onSuccess { wallet ->
                    Timber.i("Wallet imported successfully: ID=${wallet.id}, Address=${wallet.address.take(8)}...")
                    _importState.value = DecagonLoadingState.Success(
                        OnboardingState.WalletCreated(
                            walletId = wallet.id,
                            walletName = wallet.name,
                            mnemonic = mnemonic,
                            address = wallet.address
                        )
                    )
                    Timber.d("Import state set to Success (WalletCreated).")
                }
                .onFailure { error ->
                    Timber.e(error, "FATAL: Failed to import wallet")
                    _importState.value = DecagonLoadingState.Error(
                        error,
                        error.message ?: "Failed to import wallet"
                    )
                    Timber.d("Import state set to Error: ${error.message}")
                }
        }
    }

    fun validateMnemonic(phrase: String): ValidationState {
        Timber.d("Validating mnemonic phrase...")

        val trimmedPhrase = phrase.trim()
        val wordCount = importWalletUseCase.getWordCount(trimmedPhrase)
        Timber.d("Mnemonic word count: $wordCount")

        if (wordCount == null) {
            Timber.w("Validation failed: Must be 12 or 24 words.")
            return ValidationState.Invalid("Must be 12 or 24 words")
        }

        return importWalletUseCase.validatePhrase(trimmedPhrase)
            .fold(
                onSuccess = {
                    Timber.i("Mnemonic phrase is valid.")
                    ValidationState.Valid
                },
                onFailure = { error ->
                    Timber.w(error, "Mnemonic validation failed: ${error.message}")
                    ValidationState.Invalid(
                        error.message ?: "Invalid recovery phrase"
                    )
                }
            )
    }

    fun acknowledgeBackup() {
        Timber.d("Attempting to acknowledge mnemonic backup...")
        val currentState = _uiState.value
        if (currentState is DecagonLoadingState.Success) {
            val current = currentState.data as? OnboardingState.WalletCreated
            if (current != null) {
                _uiState.value = DecagonLoadingState.Success(
                    current.copy(backupAcknowledged = true)
                )
                Timber.i("Backup successfully acknowledged for wallet: ${current.walletId}")
            } else {
                Timber.e("Failed to acknowledge backup: Current state is Success but data is not WalletCreated.")
            }
        } else {
            Timber.e("Failed to acknowledge backup: Current state is not Success, but $currentState")
        }
    }

    sealed interface OnboardingState {
        data class WalletCreated(
            val walletId: String,
            val walletName: String,
            val mnemonic: String,
            val address: String,
            val backupAcknowledged: Boolean = false
        ) : OnboardingState
    }
}

sealed interface ValidationState {
    data object None : ValidationState
    data object Valid : ValidationState
    data class Invalid(val message: String) : ValidationState
}