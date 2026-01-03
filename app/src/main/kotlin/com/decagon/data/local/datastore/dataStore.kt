package com.decagon.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

// ✅ DataStore extension for Context
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * Consolidated User Preferences Store
 * Combines all settings from both old implementations
 */
interface UserPreferencesStore {
    // --- Core UI/Security Settings ---
    val currencyPreference: Flow<String>
    val privacyMode: Flow<Boolean>
    val showBalance: Flow<Boolean>
    val biometricEnabled: Flow<Boolean>
    val autoLockEnabled: Flow<Boolean>
    val autoLockTimeout: Flow<String>
    val theme: Flow<String>
    val language: Flow<String>

    // --- Network Settings ---
    val selectedNetwork: Flow<String>
    val customRpcUrl: Flow<String?>
    val testnetEnabled: Flow<Boolean>
    val rpcEndpoint: Flow<String>

    // --- Domain-Specific Settings ---
    val lastActiveWalletId: Flow<String?>
    val selectedChainId: Flow<String>
    val hideZeroBalances: Flow<Boolean>
    val sortBy: Flow<String>

    // --- Notification Settings ---
    val notificationsEnabled: Flow<Boolean>
    val priceAlertsEnabled: Flow<Boolean>

    // --- Setters (Core) ---
    suspend fun setCurrency(currency: String)
    suspend fun setPrivacyMode(enabled: Boolean)
    suspend fun setShowBalance(show: Boolean)
    suspend fun setBiometricEnabled(enabled: Boolean)
    suspend fun setAutoLockEnabled(enabled: Boolean)
    suspend fun setAutoLockTimeout(minutes: String)
    suspend fun setTheme(theme: String)
    suspend fun setLanguage(language: String)

    // --- Setters (Network) ---
    suspend fun setSelectedNetwork(network: String)
    suspend fun setCustomRpcUrl(url: String?)
    suspend fun setTestnetEnabled(enabled: Boolean)
    suspend fun setRpcEndpoint(endpoint: String)

    // --- Setters (Domain) ---
    suspend fun setLastActiveWalletId(walletId: String?)
    suspend fun setSelectedChainId(chainId: String)
    suspend fun setHideZeroBalances(hide: Boolean)
    suspend fun setSortBy(sortBy: String)

    // --- Setters (Notifications) ---
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setPriceAlertsEnabled(enabled: Boolean)

    // --- Utility ---
    suspend fun clearAll()
}

/**
 * Implementation using DataStore
 */
class UserPreferencesStoreImpl(
    private val context: Context
) : UserPreferencesStore {

    private object Keys {
        // Core UI/Security
        val CURRENCY = stringPreferencesKey("currency")
        val PRIVACY_MODE = booleanPreferencesKey("privacy_mode")
        val SHOW_BALANCE = booleanPreferencesKey("show_balance")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val AUTO_LOCK_ENABLED = booleanPreferencesKey("auto_lock_enabled")
        val AUTO_LOCK_TIMEOUT = stringPreferencesKey("auto_lock_timeout")
        val THEME = stringPreferencesKey("theme")
        val LANGUAGE = stringPreferencesKey("language")

        // Network
        val SELECTED_NETWORK = stringPreferencesKey("selected_network")
        val CUSTOM_RPC_URL = stringPreferencesKey("custom_rpc_url")
        val TESTNET_ENABLED = booleanPreferencesKey("testnet_enabled")
        val RPC_ENDPOINT = stringPreferencesKey("rpc_endpoint")

        // Domain
        val LAST_ACTIVE_WALLET_ID = stringPreferencesKey("last_active_wallet_id")
        val SELECTED_CHAIN_ID = stringPreferencesKey("selected_chain_id")
        val HIDE_ZERO_BALANCES = booleanPreferencesKey("hide_zero_balances")
        val SORT_BY = stringPreferencesKey("sort_by")

        // Notifications
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val PRICE_ALERTS_ENABLED = booleanPreferencesKey("price_alerts_enabled")
    }

    // --- Core UI/Security Flows ---

    override val currencyPreference: Flow<String> = context.dataStore.data
        .map { it[Keys.CURRENCY] ?: "USD" }

    override val privacyMode: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.PRIVACY_MODE] ?: false }

    override val showBalance: Flow<Boolean> = context.dataStore.data
        .map { prefs ->
            val privacyEnabled = prefs[Keys.PRIVACY_MODE] ?: false
            val showBalanceEnabled = prefs[Keys.SHOW_BALANCE] ?: true
            !privacyEnabled && showBalanceEnabled
        }

    override val biometricEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.BIOMETRIC_ENABLED] ?: false }

    override val autoLockEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.AUTO_LOCK_ENABLED] ?: false }

    override val autoLockTimeout: Flow<String> = context.dataStore.data
        .map { it[Keys.AUTO_LOCK_TIMEOUT] ?: "5" }

    override val theme: Flow<String> = context.dataStore.data
        .map { it[Keys.THEME] ?: "AUTO" }

    override val language: Flow<String> = context.dataStore.data
        .map { it[Keys.LANGUAGE] ?: "en" }

    // --- Network Flows ---

    override val selectedNetwork: Flow<String> = context.dataStore.data
        .map { it[Keys.SELECTED_NETWORK] ?: "mainnet-beta" }

    override val customRpcUrl: Flow<String?> = context.dataStore.data
        .map { it[Keys.CUSTOM_RPC_URL] }

    override val testnetEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.TESTNET_ENABLED] ?: false }

    override val rpcEndpoint: Flow<String> = context.dataStore.data
        .map { it[Keys.RPC_ENDPOINT] ?: "DEFAULT" }

    // --- Domain Flows ---

    override val lastActiveWalletId: Flow<String?> = context.dataStore.data
        .map { it[Keys.LAST_ACTIVE_WALLET_ID] }

    override val selectedChainId: Flow<String> = context.dataStore.data
        .map { it[Keys.SELECTED_CHAIN_ID] ?: "solana" }

    override val hideZeroBalances: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.HIDE_ZERO_BALANCES] ?: false }

    override val sortBy: Flow<String> = context.dataStore.data
        .map { it[Keys.SORT_BY] ?: "VALUE_DESC" }

    // --- Notification Flows ---

    override val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.NOTIFICATIONS_ENABLED] ?: true }

    override val priceAlertsEnabled: Flow<Boolean> = context.dataStore.data
        .map { it[Keys.PRICE_ALERTS_ENABLED] ?: true }

    // --- Setters (Core) ---

    override suspend fun setCurrency(currency: String) {
        context.dataStore.edit { it[Keys.CURRENCY] = currency }
    }

    override suspend fun setPrivacyMode(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PRIVACY_MODE] = enabled }
    }

    override suspend fun setShowBalance(show: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_BALANCE] = show }
    }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BIOMETRIC_ENABLED] = enabled }
    }

    override suspend fun setAutoLockEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.AUTO_LOCK_ENABLED] = enabled }
    }

    override suspend fun setAutoLockTimeout(minutes: String) {
        context.dataStore.edit { it[Keys.AUTO_LOCK_TIMEOUT] = minutes }
    }

    override suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[Keys.THEME] = theme }
    }

    override suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    // --- Setters (Network) ---

    override suspend fun setSelectedNetwork(network: String) {
        context.dataStore.edit { it[Keys.SELECTED_NETWORK] = network }
    }

    override suspend fun setCustomRpcUrl(url: String?) {
        context.dataStore.edit { prefs ->
            if (url != null) {
                prefs[Keys.CUSTOM_RPC_URL] = url
            } else {
                prefs.remove(Keys.CUSTOM_RPC_URL)
            }
        }
    }

    override suspend fun setTestnetEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.TESTNET_ENABLED] = enabled }
    }

    override suspend fun setRpcEndpoint(endpoint: String) {
        context.dataStore.edit { it[Keys.RPC_ENDPOINT] = endpoint }
    }

    // --- Setters (Domain) ---

    override suspend fun setLastActiveWalletId(walletId: String?) {
        context.dataStore.edit { prefs ->
            if (walletId != null) {
                prefs[Keys.LAST_ACTIVE_WALLET_ID] = walletId
            } else {
                prefs.remove(Keys.LAST_ACTIVE_WALLET_ID)
            }
        }
    }

    override suspend fun setSelectedChainId(chainId: String) {
        context.dataStore.edit { it[Keys.SELECTED_CHAIN_ID] = chainId }
    }

    override suspend fun setHideZeroBalances(hide: Boolean) {
        context.dataStore.edit { it[Keys.HIDE_ZERO_BALANCES] = hide }
    }

    override suspend fun setSortBy(sortBy: String) {
        context.dataStore.edit { it[Keys.SORT_BY] = sortBy }
    }

    // --- Setters (Notifications) ---

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    override suspend fun setPriceAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.PRICE_ALERTS_ENABLED] = enabled }
    }

    // --- Utility ---

    override suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}

/**
 * Koin Module for DataStore
 */
val dataStoreModule = module {
    single<UserPreferencesStore> {
        UserPreferencesStoreImpl(androidContext())
    }
}