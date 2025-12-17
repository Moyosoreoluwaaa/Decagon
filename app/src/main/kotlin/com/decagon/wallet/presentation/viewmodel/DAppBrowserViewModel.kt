package com.octane.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.data.local.datastore.DAppPreferencesStore
import com.octane.wallet.domain.models.Wallet
import com.octane.wallet.domain.usecases.wallet.ObserveActiveWalletUseCase
import com.octane.wallet.domain.usecases.wallet.ObserveWalletsUseCase
import com.octane.wallet.domain.usecases.wallet.SetActiveWalletUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DAppBrowserViewModel(
    private val observeActiveWalletUseCase: ObserveActiveWalletUseCase,
    private val oObserveWalletsUseCase: ObserveWalletsUseCase,
    private val setActiveWalletUseCase: SetActiveWalletUseCase,
    private val dappPreferencesStore: DAppPreferencesStore
) : ViewModel() {

    // ==================== STATE ====================

    private val _uiState = MutableStateFlow(DAppBrowserUiState())
    val uiState: StateFlow<DAppBrowserUiState> = _uiState.asStateFlow()

    val activeWallet: StateFlow<Wallet?> = observeActiveWalletUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    val allWallets: StateFlow<LoadingState<List<Wallet>>> = oObserveWalletsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading // Start with loading
        )

    // ==================== TABS ====================

    fun openTab(url: String, title: String) {
        val newTab = BrowserTab(
            id = generateTabId(),
            url = url,
            title = title,
            favicon = null
        )

        _uiState.update { state ->
            state.copy(
                tabs = state.tabs + newTab,
                activeTabId = newTab.id
            )
        }
    }

    fun closeTab(tabId: String) {
        _uiState.update { state ->
            val newTabs = state.tabs.filter { it.id != tabId }
            val newActiveId = if (state.activeTabId == tabId) {
                newTabs.lastOrNull()?.id
            } else {
                state.activeTabId
            }

            state.copy(
                tabs = newTabs,
                activeTabId = newActiveId
            )
        }
    }

    fun switchTab(tabId: String) {
        _uiState.update { it.copy(activeTabId = tabId) }
    }

    fun updateTabTitle(tabId: String, title: String) {
        _uiState.update { state ->
            state.copy(
                tabs = state.tabs.map { tab ->
                    if (tab.id == tabId) tab.copy(title = title) else tab
                }
            )
        }
    }

    // ==================== WALLET CONNECTION ====================

    /**
     * Saves the wallet connection preference for a DApp.
     * Uses the explicitly provided wallet, or defaults to the currently active wallet.
     */
    fun connectWallet(dappUrl: String, wallet: Wallet? = null) { // <--- FIXED SIGNATURE
        viewModelScope.launch {
            // Use the provided wallet, or fallback to the current active wallet
            val walletToConnect = wallet ?: activeWallet.value

            walletToConnect?.let { connectedWallet -> // <--- Use walletToConnect
                // Save connection preference
                dappPreferencesStore.saveWalletConnection(
                    dappUrl = dappUrl,
                    walletId = connectedWallet.id,
                    publicKey = connectedWallet.publicKey
                )

                _uiState.update { it.copy(isWalletConnected = true) }
            }
        }
    }

    fun disconnectWallet(dappUrl: String) {
        viewModelScope.launch {
            dappPreferencesStore.removeWalletConnection(dappUrl)
            _uiState.update { it.copy(isWalletConnected = false) }
        }
    }

    suspend fun getConnectedWallet(dappUrl: String): ConnectedWallet? {
        return dappPreferencesStore.getWalletConnection(dappUrl)
    }

    /**
     * Handles wallet selection from the switcher.
     * 1. Sets the newly selected wallet as active in the database.
     * 2. Re-runs connection logic for the current DApp using the newly active wallet.
     */
    fun onWalletSelectedForSwitch(wallet: Wallet, dappUrl: String) {
        viewModelScope.launch {
            // 1. Set the new wallet as active in the repository
            setActiveWalletUseCase(wallet.id)

            // 2. Re-connect the DApp with the newly selected wallet
            connectWallet(dappUrl, wallet) // <--- FIXED: Passing the wallet explicitly
        }
    }

    // ==================== BOOKMARKS ====================

    fun toggleBookmark(url: String, title: String) {
        viewModelScope.launch {
            val isBookmarked = dappPreferencesStore.isBookmarked(url)
            if (isBookmarked) {
                dappPreferencesStore.removeBookmark(url)
            } else {
                dappPreferencesStore.addBookmark(url, title)
            }
        }
    }

    fun observeBookmarks(): Flow<List<Bookmark>> {
        return dappPreferencesStore.observeBookmarks()
    }

    // ==================== HISTORY ====================

    fun addToHistory(url: String, title: String) {
        viewModelScope.launch {
            dappPreferencesStore.addHistory(
                url = url,
                title = title,
                timestamp = System.currentTimeMillis()
            )
        }
    }

    // ==================== OPTIONS ====================

    fun showOptionsMenu() {
        _uiState.update { it.copy(showOptionsMenu = true) }
    }

    fun hideOptionsMenu() {
        _uiState.update { it.copy(showOptionsMenu = false) }
    }

    fun showWalletSwitcher() {
        _uiState.update { it.copy(showWalletSwitcher = true) }
    }

    fun hideWalletSwitcher() {
        _uiState.update { it.copy(showWalletSwitcher = false) }
    }

    private fun generateTabId() = "tab_${System.currentTimeMillis()}"
}

// ==================== DATA MODELS ====================

data class DAppBrowserUiState(
    val tabs: List<BrowserTab> = emptyList(),
    val activeTabId: String? = null,
    val isWalletConnected: Boolean = false,
    val showOptionsMenu: Boolean = false,
    val showWalletSwitcher: Boolean = false,
    val showBookmarksSheet: Boolean = false
)

data class BrowserTab(
    val id: String,
    val url: String,
    val title: String,
    val favicon: String?
)

data class ConnectedWallet(
    val walletId: String,
    val publicKey: String,
    val connectedAt: Long
)

data class Bookmark(
    val url: String,
    val title: String,
    val favicon: String?,
    val addedAt: Long
)

data class HistoryItem(
    val url: String,
    val title: String,
    val timestamp: Long
)