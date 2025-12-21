package com.octane.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.presentation.viewmodel.BasePortfolioViewModel
import com.wallet.presentation.viewmodel.BaseWalletViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Receive screen ViewModel.
 * Handles address display, QR code generation, token-specific receives.
 *
 * RESPONSIBILITY:
 * - Display wallet address (full and truncated)
 * - Generate QR code for address
 * - Token-specific receive addresses (SPL tokens)
 * - Share functionality
 * - Copy to clipboard with haptic feedback
 *
 * Pattern: features/receive/ReceiveViewModel.kt
 */
class ReceiveViewModel(
    private val baseWallet: BaseWalletViewModel,
    private val basePortfolio: BasePortfolioViewModel,
    private val qrCodeGenerator: QRCodeGenerator // You'll need to create this
) : ViewModel() {

    // UI State: Receive screen
    private val _receiveState = MutableStateFlow(ReceiveState())
    val receiveState: StateFlow<ReceiveState> = _receiveState.asStateFlow()

    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet

    // Delegate: Portfolio (for token selection)
    val portfolioState = basePortfolio.portfolioState

    // One-time events
    private val _events = MutableSharedFlow<ReceiveEvent>(replay = 0)
    val events: SharedFlow<ReceiveEvent> = _events.asSharedFlow()

    init {
        observeActiveWallet()
    }

    /**
     * Observe active wallet and generate QR code.
     */
    private fun observeActiveWallet() {
        viewModelScope.launch {
            activeWallet.collect { wallet ->
                if (wallet != null) {
                    generateQRCode(wallet.publicKey)
                    _receiveState.update {
                        it.copy(
                            address = wallet.publicKey,
                            truncatedAddress = truncateAddress(wallet.publicKey)
                        )
                    }
                }
            }
        }
    }

    /**
     * Select token to receive (changes instructions).
     */
    fun selectToken(tokenSymbol: String) {
        _receiveState.update { it.copy(selectedToken = tokenSymbol) }

        // For SPL tokens, show token-specific instructions
        if (tokenSymbol != "SOL") {
            _receiveState.update {
                it.copy(
                    instructionMessage = "Send $tokenSymbol to this Solana address. Your wallet will automatically create a token account if needed."
                )
            }
        } else {
            _receiveState.update {
                it.copy(
                    instructionMessage = "Send SOL to this address from any wallet or exchange."
                )
            }
        }
    }

    /**
     * Generate QR code bitmap from address.
     */
    private fun generateQRCode(address: String) {
        viewModelScope.launch {
            try {
                _receiveState.update { it.copy(isGeneratingQR = true) }

                // Generate QR code (you'll implement this)
                val qrBitmap = qrCodeGenerator.generate(
                    content = address,
                    size = 512, // 512x512 pixels
                    foregroundColor = android.graphics.Color.BLACK,
                    backgroundColor = android.graphics.Color.WHITE
                )

                _receiveState.update {
                    it.copy(
                        qrCodeBitmap = qrBitmap,
                        isGeneratingQR = false
                    )
                }
            } catch (e: Exception) {
                _receiveState.update { it.copy(isGeneratingQR = false) }
                _events.emit(ReceiveEvent.Error("Failed to generate QR code"))
            }
        }
    }

    /**
     * Copy address to clipboard.
     */
    fun copyAddress() {
        viewModelScope.launch {
            _events.emit(ReceiveEvent.AddressCopied(_receiveState.value.address))
        }
    }

    /**
     * Share address via system share sheet.
     */
    fun shareAddress() {
        viewModelScope.launch {
            _events.emit(ReceiveEvent.ShareAddress(_receiveState.value.address))
        }
    }

    /**
     * Share QR code image.
     */
    fun shareQRCode() {
        viewModelScope.launch {
            val bitmap = _receiveState.value.qrCodeBitmap
            if (bitmap != null) {
                _events.emit(ReceiveEvent.ShareQRCode(bitmap))
            } else {
                _events.emit(ReceiveEvent.Error("QR code not ready"))
            }
        }
    }

    /**
     * Truncate address for display.
     * Example: "ABC...XYZ" (4 chars + ... + 4 chars)
     */
    private fun truncateAddress(address: String): String {
        if (address.length <= 12) return address
        return "${address.take(4)}...${address.takeLast(4)}"
    }
}

/**
 * Receive screen state.
 */
data class ReceiveState(
    val address: String = "",
    val truncatedAddress: String = "",
    val qrCodeBitmap: android.graphics.Bitmap? = null,
    val selectedToken: String = "SOL",
    val instructionMessage: String = "Send SOL to this address from any wallet or exchange.",
    val isGeneratingQR: Boolean = false
)

/**
 * Receive events.
 */
sealed interface ReceiveEvent {
    data class AddressCopied(val address: String) : ReceiveEvent
    data class ShareAddress(val address: String) : ReceiveEvent
    data class ShareQRCode(val bitmap: android.graphics.Bitmap) : ReceiveEvent
    data class Error(val message: String) : ReceiveEvent
}

/**
 * QR Code Generator interface (implement in infrastructure layer).
 */
interface QRCodeGenerator {
    suspend fun generate(
        content: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): android.graphics.Bitmap
}