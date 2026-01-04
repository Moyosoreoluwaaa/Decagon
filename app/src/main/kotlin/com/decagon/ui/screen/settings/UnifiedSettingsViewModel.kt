package com.decagon.ui.screen.settings

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.BuildConfig
import com.decagon.core.network.ConnectionType
import com.decagon.core.network.RpcClientFactory
import com.decagon.core.util.DecagonLoadingState
import com.decagon.data.local.datastore.UserPreferencesStore
import com.decagon.domain.repository.DecagonSettingsRepository
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.usecase.network.NetworkStatus
import com.decagon.domain.usecase.network.ObserveNetworkStatusUseCase
import com.decagon.domain.usecase.preference.*
import com.decagon.domain.usecase.security.CheckBiometricAvailabilityUseCase
import com.decagon.util.TransactionDiagnostic
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class UnifiedSettingsViewModel(
    // Repositories
    private val settingsRepository: DecagonSettingsRepository,
    private val walletRepository: DecagonWalletRepository,
    private val transactionRepository: DecagonTransactionRepository,

    // Use Cases
    private val updateCurrencyUseCase: UpdateCurrencyPreferenceUseCase,
    private val togglePrivacyModeUseCase: TogglePrivacyModeUseCase,
    private val observeCurrencyUseCase: ObserveCurrencyPreferenceUseCase,
    private val checkBiometricUseCase: CheckBiometricAvailabilityUseCase,
    private val observeNetworkStatusUseCase: ObserveNetworkStatusUseCase,

    // Services
    private val rpcFactory: RpcClientFactory,
    private val userPreferencesStore: UserPreferencesStore,
) : ViewModel() {
    // ==================== APP-WIDE PREFERENCES ====================

    val selectedCurrency = observeCurrencyUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "USD")

    val privacyMode = userPreferencesStore.privacyMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val biometricEnabled = userPreferencesStore.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val testnetEnabled = userPreferencesStore.testnetEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val autoLockEnabled = userPreferencesStore.autoLockEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val autoLockTimeout = userPreferencesStore.autoLockTimeout
        .stateIn(viewModelScope, SharingStarted.Eagerly, "300")

    val theme = userPreferencesStore.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, "AUTO")

    // Add to UnifiedSettingsViewModel
    val networkStatus = observeNetworkStatusUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            NetworkStatus(true, ConnectionType.WIFI, false))


    // ==================== WALLET ACTIONS ====================

    /**
     * ✅ NO setActivity() - Pass activity directly
     */

    fun updateWalletName(walletId: String, newName: String) {
        viewModelScope.launch {
            settingsRepository.updateWalletName(walletId, newName)
        }
    }

    fun removeWallet(walletId: String, activity: FragmentActivity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.removeWallet(walletId, activity)
                .onSuccess { onSuccess() }
                .onFailure { Timber.e(it, "Failed to remove wallet") }
        }
    }

    fun toggleTestnet(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setTestnetEnabled(enabled)
        }
    }

    // ==================== APP PREFERENCES ====================

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            updateCurrencyUseCase(currency)
        }
    }

    fun togglePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            togglePrivacyModeUseCase(enabled)
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setBiometricEnabled(enabled)
            if (!enabled) {
                // Disable auto-lock if biometric disabled
                userPreferencesStore.setAutoLockEnabled(false)
            }
        }
    }

    fun toggleAutoLock(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setAutoLockEnabled(enabled)
        }
    }

    fun setAutoLockTimeout(seconds: Int) {
        viewModelScope.launch {
            userPreferencesStore.setAutoLockTimeout(seconds.toString())
        }
    }

    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesStore.setTheme(theme.name)
        }
    }

    fun getAppVersion(): String = BuildConfig.VERSION_NAME

    /**
     * Open support/feedback.
     */
    fun openSupport() {
        // TODO: Open support URL
    }

    /**
     * Open terms of service.
     */
    fun openTerms() {
        // TODO: Open terms URL
    }

    /**
     * Open privacy policy.
     */
    fun openPrivacyPolicy() {
        // TODO: Open privacy URL
    }

    // ==================== DIAGNOSTICS ====================

    fun fixStuckTransactions(walletAddress: String) {
        viewModelScope.launch {
            val diagnostic = TransactionDiagnostic(
                transactionRepository = transactionRepository,
                rpcFactory = rpcFactory,
                walletRepository = walletRepository
            )
            diagnostic.diagnoseAndFixPending(walletAddress)
        }
    }
}

enum class AppTheme {
    LIGHT, DARK, AUTO
}