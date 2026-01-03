package com.decagon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.domain.usecase.network.ObserveNetworkStatusUseCase
import com.decagon.domain.usecase.preference.ObserveCurrencyPreferenceUseCase
import com.decagon.domain.usecase.preference.TogglePrivacyModeUseCase
import com.decagon.domain.usecase.preference.UpdateCurrencyPreferenceUseCase
import com.decagon.domain.usecase.security.CheckBiometricAvailabilityUseCase
import com.decagon.core.network.ConnectionType
import com.decagon.core.security.BiometricAvailability
import com.decagon.data.local.datastore.UserPreferencesStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Settings screen ViewModel.
 * Handles app-wide preferences, security, network, appearance.
 *
 * RESPONSIBILITY:
 * - User preferences (currency, privacy mode)
 * - Security settings (biometrics, auto-lock)
 * - Network settings (RPC endpoint, testnet/mainnet)
 * - Appearance settings (theme, language)
 * - Account management (backup, export)
 *
 * Pattern: features/settings/SettingsViewModel.kt
 */
class SettingsViewModel(
    private val updateCurrencyUseCase: UpdateCurrencyPreferenceUseCase,
    private val togglePrivacyModeUseCase: TogglePrivacyModeUseCase,
    private val observeNetworkStatusUseCase: ObserveNetworkStatusUseCase,
    private val observeCurrencyUseCase: ObserveCurrencyPreferenceUseCase,
    private val checkBiometricUseCase: CheckBiometricAvailabilityUseCase,
    private val userPreferencesStore: UserPreferencesStore // For observing all preferences
) : ViewModel() {

    // UI State: Settings data
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    // Observe preferences
    val selectedCurrency = observeCurrencyUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, "USD")

    val networkStatus = observeNetworkStatusUseCase()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            NetworkStatus(true, ConnectionType.WIFI, false)
        )

    val privacyMode = userPreferencesStore.privacyMode
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        checkBiometricAvailability()
    }

    /**
     * Check if biometrics are available.
     */
    private fun checkBiometricAvailability() {
        val availability = checkBiometricUseCase()
        _settingsState.update {
            it.copy(biometricAvailable = availability == BiometricAvailability.Available)
        }
    }

    // ==================== GENERAL SETTINGS ====================

    /**
     * Update currency preference.
     */
    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            updateCurrencyUseCase(currency)
        }
    }

    /**
     * Toggle privacy mode (hide balances).
     */
    fun togglePrivacyMode(enabled: Boolean) {
        viewModelScope.launch {
            togglePrivacyModeUseCase(enabled)
        }
    }

    // ==================== SECURITY SETTINGS ====================

    /**
     * Toggle biometric authentication.
     */
    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setBiometricEnabled(enabled)
            _settingsState.update { it.copy(biometricEnabled = enabled) }
        }
    }

    /**
     * Set auto-lock timeout (seconds).
     */
    fun setAutoLockTimeout(seconds: Int) {
        viewModelScope.launch {
            userPreferencesStore.setAutoLockTimeout(seconds.toString())
            _settingsState.update { it.copy(autoLockTimeout = seconds) }
        }
    }

    /**
     * Show seed phrase (requires biometric auth).
     */
    fun requestShowSeedPhrase() {
        _settingsState.update { it.copy(showSeedPhraseDialog = true) }
    }

    fun hideShowSeedPhrase() {
        _settingsState.update { it.copy(showSeedPhraseDialog = false) }
    }

    // ==================== NETWORK SETTINGS ====================

    /**
     * Toggle testnet/mainnet.
     */
    fun toggleTestnet(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setTestnetEnabled(enabled)
            _settingsState.update { it.copy(testnetEnabled = enabled) }
        }
    }

    // ==================== APPEARANCE SETTINGS ====================

    /**
     * Set theme (light/dark/auto).
     */
    fun setTheme(theme: AppTheme) {
        viewModelScope.launch {
            userPreferencesStore.setTheme(theme.name)
            _settingsState.update { it.copy(theme = theme) }
        }
    }

    /**
     * Set language.
     */
    fun setLanguage(language: String) {
        viewModelScope.launch {
            userPreferencesStore.setLanguage(language)
            _settingsState.update { it.copy(language = language) }
        }
    }

    // ==================== ACCOUNT MANAGEMENT ====================

    /**
     * Request backup seed phrase.
     */
    fun requestBackup() {
        _settingsState.update { it.copy(showBackupDialog = true) }
    }

    fun hideBackup() {
        _settingsState.update { it.copy(showBackupDialog = false) }
    }

    /**
     * Export private key (requires biometric auth).
     */
    fun requestExportPrivateKey() {
        _settingsState.update { it.copy(showExportKeyDialog = true) }
    }

    fun hideExportPrivateKey() {
        _settingsState.update { it.copy(showExportKeyDialog = false) }
    }

    // ==================== ABOUT ====================

    /**
     * Get app version.
     */
    fun getAppVersion(): String {
        return "1.0.0" // TODO: Get from BuildConfig
    }

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
}

/**
 * Settings state.
 */
data class SettingsState(
    // Security
    val biometricAvailable: Boolean = false,
    val biometricEnabled: Boolean = false,
    val autoLockTimeout: Int = 300, // 5 minutes

    // Network
    val testnetEnabled: Boolean = false,

    // Appearance
    val theme: AppTheme = AppTheme.AUTO,
    val language: String = "en",

    // Dialogs
    val showSeedPhraseDialog: Boolean = false,
    val showBackupDialog: Boolean = false,
    val showExportKeyDialog: Boolean = false
)

/**
 * App theme options.
 */
enum class AppTheme {
    LIGHT, DARK, AUTO
}