package com.decagon.ui.screen.onboarding

import android.annotation.SuppressLint
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.usecase.DecagonCreateWalletUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber // Import added

class DecagonOnboardingViewModel(
    private val createWalletUseCase: DecagonCreateWalletUseCase,
    private val biometricAuthenticator: DecagonBiometricAuthenticator
) : ViewModel() {

    @SuppressLint("StaticFieldLeak")
    private var currentActivity: FragmentActivity? = null

    fun setActivity(activity: FragmentActivity?) {
        currentActivity = activity
        biometricAuthenticator.setActivity(activity)
    }

    private val _uiState = MutableStateFlow<DecagonLoadingState<OnboardingState>>(
        DecagonLoadingState.Idle
    )
    val uiState: StateFlow<DecagonLoadingState<OnboardingState>> = _uiState.asStateFlow()

    init {
        Timber.d("DecagonOnboardingViewModel initialized.") // Log added
        checkBiometricStatus()
    }

    private fun checkBiometricStatus() {
        Timber.d("Checking biometric status.") // Log added
        val status = biometricAuthenticator.checkBiometricStatus()
        if (!status.isAvailable) {
            Timber.w("Biometric not available: ${status.getUserMessage()}") // Log added
            _uiState.value = DecagonLoadingState.Error(
                IllegalStateException("Biometric unavailable"),
                status.getUserMessage()
            )
        } else {
            Timber.i("Biometric is available.") // Log added
        }
    }

    fun createWallet(name: String) {  // ✅ Remove activity param
        val activity = currentActivity ?: run {
            Timber.e("Cannot create wallet: Activity is null")
            _uiState.value = DecagonLoadingState.Error(
                IllegalStateException("Activity required"),
                "Cannot access biometric authentication"
            )
            return
        }

        Timber.i("Attempting to create wallet with name: $name")
        viewModelScope.launch {  // ✅ Launches on Main by default
            _uiState.value = DecagonLoadingState.Loading

            try {
                val result = createWalletUseCase(name, activity).getOrThrow()
                Timber.i("Wallet created successfully: ${result.wallet.id}")

                _uiState.value = DecagonLoadingState.Success(
                    OnboardingState.WalletCreated(
                        walletId = result.wallet.id,
                        walletName = result.wallet.name,
                        mnemonic = result.mnemonic,
                        address = result.wallet.address
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Error creating wallet: $name")
                _uiState.value = DecagonLoadingState.Error(e, "Failed to create wallet: ${e.message}")
            }
        }
    }

    fun acknowledgeBackup() {
        Timber.d("Acknowledging mnemonic backup.") // Log added
        val currentState = _uiState.value
        if (currentState is DecagonLoadingState.Success) {
            val current = currentState.data as? OnboardingState.WalletCreated ?: return
            _uiState.value = DecagonLoadingState.Success(
                current.copy(backupAcknowledged = true)
            )
            Timber.i("Backup acknowledged for wallet: ${current.walletId}") // Log added
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