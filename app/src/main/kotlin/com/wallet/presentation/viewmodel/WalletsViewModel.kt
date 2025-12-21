package com.octane.wallet.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Wallet
import com.wallet.presentation.viewmodel.BaseWalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber

/**
 * Wallets screen ViewModel.
 * Extends BaseWalletViewModel with UI-specific state.
 *
 * RESPONSIBILITY:
 * - Display wallet list with balances
 * - Create/import/delete flows
 * - Wallet switcher UI state
 * - Edit wallet metadata (name, emoji, color)
 *
 * Pattern: features/wallets/WalletsViewModel.kt
 */

/**
 * Wallets screen ViewModel.
 * Extends BaseWalletViewModel with UI-specific state.
 */
class WalletsViewModel(
    private val baseWallet: BaseWalletViewModel
) : ViewModel() {

    init {
        Timber.d("🔵 [WalletsVM] ViewModel initialized")
    }

    // Delegate: Wallet list
    val walletsState = baseWallet.walletsState

    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet

    // Delegate: Wallet events
    val walletEvents = baseWallet.events

    // UI State: Bottom sheets
    private val _uiState = MutableStateFlow(WalletsUiState())
    val uiState: StateFlow<WalletsUiState> = _uiState.asStateFlow()

    /**
     * Show create wallet sheet.
     */
    fun showCreateWallet() {
        Timber.d("🔵 [WalletsVM] showCreateWallet called")
        _uiState.update { it.copy(showCreateSheet = true) }
        Timber.d("✅ [WalletsVM] showCreateSheet = true")
    }

    fun hideCreateWallet() {
        Timber.d("🔵 [WalletsVM] hideCreateWallet called")
        _uiState.update { it.copy(showCreateSheet = false) }
        Timber.d("✅ [WalletsVM] showCreateSheet = false")
    }

    /**
     * Show import wallet sheet.
     */
    fun showImportWallet() {
        Timber.d("🔵 [WalletsVM] showImportWallet called")
        _uiState.update { it.copy(showImportSheet = true) }
    }

    fun hideImportWallet() {
        Timber.d("🔵 [WalletsVM] hideImportWallet called")
        _uiState.update { it.copy(showImportSheet = false) }
    }

    /**
     * Show delete confirmation dialog.
     */
    fun showDeleteConfirmation(walletId: String) {
        Timber.d("🔵 [WalletsVM] showDeleteConfirmation: $walletId")
        _uiState.update {
            it.copy(
                showDeleteConfirmation = true,
                walletToDelete = walletId
            )
        }
    }

    fun hideDeleteConfirmation() {
        Timber.d("🔵 [WalletsVM] hideDeleteConfirmation called")
        _uiState.update {
            it.copy(
                showDeleteConfirmation = false,
                walletToDelete = null
            )
        }
    }

    /**
     * Show edit wallet sheet.
     */
    fun showEditWallet(walletId: String) {
        Timber.d("🔵 [WalletsVM] showEditWallet: $walletId")
        val wallet = (walletsState.value as? LoadingState.Success)
            ?.data?.find { it.id == walletId }

        if (wallet != null) {
            Timber.d("✅ [WalletsVM] Wallet found: ${wallet.name}")
            _uiState.update {
                it.copy(
                    showEditSheet = true,
                    editingWallet = wallet
                )
            }
        } else {
            Timber.w("⚠️ [WalletsVM] Wallet not found: $walletId")
        }
    }

    fun hideEditWallet() {
        Timber.d("🔵 [WalletsVM] hideEditWallet called")
        _uiState.update {
            it.copy(
                showEditSheet = false,
                editingWallet = null
            )
        }
    }

    /**
     * Create new wallet (delegates to base).
     */
    fun createWallet(
        name: String,
        iconEmoji: String?,
        colorHex: String?
    ) {
        Timber.d("========================================")
        Timber.d("🔵 [WalletsVM] createWallet called")
        Timber.d("🔵 [WalletsVM] name='$name' (${name.length} chars)")
        Timber.d("🔵 [WalletsVM] iconEmoji=$iconEmoji")
        Timber.d("🔵 [WalletsVM] colorHex=$colorHex")
        Timber.d("========================================")

        Timber.d("🔵 [WalletsVM] Delegating to baseWallet.createWallet()...")
        try {
            baseWallet.createWallet(name, iconEmoji, colorHex)
            Timber.d("✅ [WalletsVM] Delegated to BaseWalletViewModel")
        } catch (e: Exception) {
            Timber.e(e, "❌ [WalletsVM] Exception while delegating: ${e.message}")
        }

        Timber.d("🔵 [WalletsVM] Hiding create wallet sheet...")
        hideCreateWallet()
        Timber.d("✅ [WalletsVM] createWallet completed")
    }

    /**
     * Import existing wallet (delegates to base).
     */
    fun importWallet(
        seedPhrase: String,
        name: String,
        iconEmoji: String?,
        colorHex: String?
    ) {
        Timber.d("🔵 [WalletsVM] importWallet: name=$name, phrase=${seedPhrase.split(" ").size} words")
        baseWallet.importWallet(seedPhrase, name, iconEmoji, colorHex)
        hideImportWallet()
    }

    /**
     * Delete wallet with confirmation.
     */
    fun deleteWallet() {
        val walletId = _uiState.value.walletToDelete
        Timber.d("🔵 [WalletsVM] deleteWallet: $walletId")

        if (walletId != null) {
            baseWallet.deleteWallet(walletId)
            hideDeleteConfirmation()
        } else {
            Timber.w("⚠️ [WalletsVM] deleteWallet called but walletToDelete is null")
        }
    }

    /**
     * Switch active wallet (delegates to base).
     */
    fun switchWallet(walletId: String) {
        Timber.d("🔵 [WalletsVM] switchWallet: $walletId")
        baseWallet.switchWallet(walletId)
    }

    /**
     * Update wallet metadata.
     */
    fun updateWallet(
        walletId: String,
        name: String?,
        iconEmoji: String?,
        colorHex: String?
    ) {
        Timber.d("🔵 [WalletsVM] updateWallet: $walletId")
        baseWallet.updateWalletMetadata(walletId, name, iconEmoji, colorHex)
        hideEditWallet()
    }

    /**
     * Get wallet display name with fallback.
     */
    fun getWalletDisplayName(wallet: Wallet): String {
        return wallet.name.ifBlank { "Wallet ${wallet.id.take(4)}" }
    }

    /**
     * Get wallet color as Compose Color.
     */
    fun getWalletColor(wallet: Wallet): Color {
        return try {
            Color(android.graphics.Color.parseColor(wallet.colorHex))
        } catch (e: Exception) {
            Color(0xFF4ECDC4) // Default teal
        }
    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("🔵 [WalletsVM] ViewModel cleared")
    }
}

/**
 * Wallets UI state (sheets, dialogs).
 */
data class WalletsUiState(
    val showCreateSheet: Boolean = false,
    val showImportSheet: Boolean = false,
    val showEditSheet: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val walletToDelete: String? = null,
    val editingWallet: Wallet? = null
)