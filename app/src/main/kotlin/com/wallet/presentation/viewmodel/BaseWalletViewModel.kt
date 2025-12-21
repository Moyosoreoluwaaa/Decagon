package com.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Wallet
import com.wallet.domain.usecases.wallet.CreateWalletUseCase
import com.wallet.domain.usecases.wallet.DeleteWalletUseCase
import com.wallet.domain.usecases.wallet.ImportWalletUseCase
import com.wallet.domain.usecases.wallet.ObserveWalletsUseCase
import com.wallet.domain.usecases.wallet.SetActiveWalletUseCase
import com.wallet.domain.usecases.wallet.UpdateWalletUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

open class BaseWalletViewModel(
    private val observeWalletsUseCase: ObserveWalletsUseCase,
    private val createWalletUseCase: CreateWalletUseCase,
    private val importWalletUseCase: ImportWalletUseCase,
    private val deleteWalletUseCase: DeleteWalletUseCase,
    private val setActiveWalletUseCase: SetActiveWalletUseCase,
    private val updateWalletUseCase: UpdateWalletUseCase
) : ViewModel() {

    val walletsState: StateFlow<LoadingState<List<Wallet>>> = observeWalletsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val activeWallet: StateFlow<Wallet?> = walletsState
        .map { state ->
            (state as? LoadingState.Success)?.data?.find { it.isActive }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _events = MutableSharedFlow<WalletEvent>(replay = 0, extraBufferCapacity = 1)
    val events: SharedFlow<WalletEvent> = _events.asSharedFlow()

    fun createWallet(
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        Timber.d("========================================")
        Timber.d("🔵 [BaseWalletVM] createWallet called")
        Timber.d("🔵 [BaseWalletVM] name='$name' (${name.length} chars)")
        Timber.d("🔵 [BaseWalletVM] iconEmoji=$iconEmoji")
        Timber.d("🔵 [BaseWalletVM] colorHex=$colorHex")
        Timber.d("========================================")

        viewModelScope.launch {
            Timber.d("🔵 [BaseWalletVM] Coroutine launched in viewModelScope")
            Timber.d("🔵 [BaseWalletVM] Thread: ${Thread.currentThread().name}")

            try {
                Timber.d("🔵 [BaseWalletVM] Calling createWalletUseCase.invoke()...")

                val result = createWalletUseCase(name, iconEmoji, colorHex)

                Timber.d("🔵 [BaseWalletVM] UseCase returned, checking result...")

                result
                    .onSuccess { creationResult ->
                        Timber.d("========================================")
                        Timber.d("✅ [BaseWalletVM] UseCase SUCCESS")
                        Timber.d("✅ [BaseWalletVM] Wallet ID: ${creationResult.wallet.id}")
                        Timber.d("✅ [BaseWalletVM] Wallet name: ${creationResult.wallet.name}")
                        Timber.d("✅ [BaseWalletVM] Seed phrase: ${creationResult.seedPhrase.split(" ").size} words")
                        Timber.d(
                            "✅ [BaseWalletVM] First 3 words: ${
                                creationResult.seedPhrase.split(
                                    " "
                                ).take(3).joinToString(" ")
                            }"
                        )
                        Timber.d("========================================")

                        Timber.d("🔵 [BaseWalletVM] Emitting WalletCreatedWithSeed event...")
                        val emitResult = _events.tryEmit(
                            WalletEvent.WalletCreatedWithSeed(
                                wallet = creationResult.wallet,
                                seedPhrase = creationResult.seedPhrase,
                                iconEmoji = iconEmoji,
                            )
                        )

                        if (emitResult) {
                            Timber.d("✅ [BaseWalletVM] Event emitted successfully: WalletCreatedWithSeed")
                        } else {
                            Timber.e("❌ [BaseWalletVM] Event emit FAILED - buffer full or no collectors")
                        }
                    }
                    .onFailure { e ->
                        Timber.e("========================================")
                        Timber.e(e, "❌ [BaseWalletVM] UseCase FAILED")
                        Timber.e(e, "❌ [BaseWalletVM] Error message: ${e.message}")
                        Timber.e(e, "❌ [BaseWalletVM] Exception type: ${e::class.simpleName}")
                        Timber.e(e, "❌ [BaseWalletVM] Stack trace: ${e.stackTraceToString()}")
                        Timber.e("========================================")

                        Timber.d("🔵 [BaseWalletVM] Emitting Error event...")
                        val emitResult = _events.tryEmit(
                            WalletEvent.Error("Failed to create wallet: ${e.message}")
                        )

                        if (emitResult) {
                            Timber.d("✅ [BaseWalletVM] Error event emitted successfully")
                        } else {
                            Timber.e("❌ [BaseWalletVM] Error event emit FAILED")
                        }
                    }

            } catch (e: Exception) {
                Timber.e("========================================")
                Timber.e(e, "❌ [BaseWalletVM] Unexpected exception in coroutine")
                Timber.e(e, "❌ [BaseWalletVM] Exception: ${e::class.simpleName}: ${e.message}")
                Timber.e(e, "❌ [BaseWalletVM] Stack: ${e.stackTraceToString()}")
                Timber.e("========================================")

                _events.tryEmit(WalletEvent.Error("Unexpected error: ${e.message}"))
            }
        }

        Timber.d("✅ [BaseWalletVM] createWallet function completed (coroutine launched)")
    }

    fun importWallet(
        seedPhrase: String,
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        Timber.d("🔵 [BaseWalletVM] importWallet called: name=$name")

        viewModelScope.launch {
            importWalletUseCase(seedPhrase, name, iconEmoji, colorHex)
                .onSuccess { wallet ->
                    Timber.d("✅ [BaseWalletVM] Import success: ${wallet.id}")
                    _events.emit(WalletEvent.WalletImported(wallet))
                }
                .onFailure { e ->
                    Timber.e(e, "❌ [BaseWalletVM] Import failed: ${e.message}")
                    _events.emit(WalletEvent.Error(e.message ?: "Import failed"))
                }
        }
    }

    fun deleteWallet(walletId: String) {
        Timber.d("🔵 [BaseWalletVM] deleteWallet called: $walletId")

        viewModelScope.launch {
            deleteWalletUseCase(walletId)
                .onSuccess {
                    Timber.d("✅ [BaseWalletVM] Delete success")
                    _events.emit(WalletEvent.WalletDeleted)
                }
                .onFailure { e ->
                    Timber.e(e, "❌ [BaseWalletVM] Delete failed: ${e.message}")
                    _events.emit(WalletEvent.Error(e.message ?: "Delete failed"))
                }
        }
    }

    fun switchWallet(walletId: String) {
        Timber.d("🔵 [BaseWalletVM] switchWallet called: $walletId")

        viewModelScope.launch {
            setActiveWalletUseCase(walletId)
                .onSuccess {
                    Timber.d("✅ [BaseWalletVM] Switch success")
                    _events.emit(WalletEvent.WalletSwitched)
                }
                .onFailure { e ->
                    Timber.e(e, "❌ [BaseWalletVM] Switch failed")
                    _events.emit(WalletEvent.Error("Failed to switch wallet"))
                }
        }
    }

    fun updateWalletMetadata(
        walletId: String,
        name: String? = null,
        iconEmoji: String? = null,
        colorHex: String? = null
    ) {
        Timber.d("🔵 [BaseWalletVM] updateWalletMetadata called: $walletId")

        viewModelScope.launch {
            updateWalletUseCase(walletId, name, iconEmoji, colorHex)
                .onSuccess {
                    Timber.d("✅ [BaseWalletVM] Update success")
                    _events.emit(WalletEvent.WalletUpdated)
                }
                .onFailure { e ->
                    Timber.e(e, "❌ [BaseWalletVM] Update failed")
                    _events.emit(WalletEvent.Error("Failed to update wallet"))
                }
        }
    }
}

sealed interface WalletEvent {
    data class WalletCreated(val wallet: Wallet) : WalletEvent
    data class WalletCreatedWithSeed(
        val wallet: Wallet,
        val seedPhrase: String,
        val iconEmoji: String?
    ) : WalletEvent

    data class WalletImported(val wallet: Wallet) : WalletEvent
    data object WalletDeleted : WalletEvent
    data object WalletSwitched : WalletEvent
    data object WalletUpdated : WalletEvent
    data class Error(val message: String) : WalletEvent
}