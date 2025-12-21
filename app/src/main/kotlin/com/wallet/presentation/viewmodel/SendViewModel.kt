package com.octane.wallet.presentation.viewmodel

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.wallet.domain.usecases.security.AuthenticateWithBiometricsUseCase
import com.octane.wallet.domain.usecases.security.ValidateSolanaAddressUseCase
import com.octane.wallet.domain.usecases.transaction.EstimateTransactionFeeUseCase
import com.octane.wallet.domain.usecases.transaction.SendTokenUseCase
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
 * Send screen ViewModel.
 * Handles SOL/token sending with biometric confirmation.
 *
 * RESPONSIBILITY:
 * - Amount/recipient input validation
 * - Fee estimation
 * - Transaction submission
 * - Biometric authentication
 *
 * Pattern: features/send/SendViewModel.kt
 */
class SendViewModel(
    private val estimateFeeUseCase: EstimateTransactionFeeUseCase,
    private val sendTokenUseCase: SendTokenUseCase,
    private val authenticateWithBiometricsUseCase: AuthenticateWithBiometricsUseCase,
    private val validateSolanaAddressUseCase: ValidateSolanaAddressUseCase,

    // Shared state
    private val basePortfolio: BasePortfolioViewModel,
    private val baseWallet: BaseWalletViewModel
) : ViewModel() {

    // UI State: Send form
    private val _sendState = MutableStateFlow(SendState())
    val sendState: StateFlow<SendState> = _sendState.asStateFlow()

    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet

    // Delegate: Portfolio (for token selection)
    val portfolioState = basePortfolio.portfolioState

    // One-time events
    private val _events = MutableSharedFlow<SendEvent>(replay = 0)
    val events: SharedFlow<SendEvent> = _events.asSharedFlow()

    /**
     * Update amount input.
     * Validates against max balance, updates fee estimate.
     */
    fun onAmountChanged(amount: String) {
        val parsed = amount.toDoubleOrNull() ?: 0.0
        _sendState.update { it.copy(amount = amount, amountUsd = parsed * it.tokenPriceUsd) }
        estimateFee()
    }

    /**
     * Update recipient address.
     * Validates Solana address format, checks for SNS domains.
     */
    fun onRecipientChanged(recipient: String) {
        viewModelScope.launch {
            val isValid = validateSolanaAddressUseCase(recipient)
            _sendState.update {
                it.copy(
                    recipient = recipient,
                    recipientValid = isValid
                )
            }
        }
    }

    /**
     * Select token to send.
     * Updates max balance, price, fee estimate.
     */
    fun selectToken(tokenSymbol: String) {
        val asset = (portfolioState.value as? LoadingState.Success)
            ?.data?.assets?.find { it.symbol == tokenSymbol }

        if (asset != null) {
            _sendState.update {
                it.copy(
                    selectedToken = tokenSymbol,
                    maxBalance = asset.balance.toDouble(),
                    tokenPriceUsd = asset.priceUsd ?: 0.0
                )
            }
            estimateFee()
        }
    }

    /**
     * Quick amount buttons (25%, 50%, 75%, Max).
     */
    fun onQuickAmountClick(percentage: Float) {
        val amount = _sendState.value.maxBalance * percentage
        onAmountChanged(amount.toString())
    }

    /**
     * Estimate transaction fee (dynamic based on network load).
     */
    private fun estimateFee() {
        viewModelScope.launch {
            val state = _sendState.value
            if (state.amount.toDoubleOrNull() == null) return@launch

            val feeResult = estimateFeeUseCase(
                tokenSymbol = state.selectedToken,
                amount = state.amount.toDouble()
            )

            feeResult.onSuccess { fee ->
                _sendState.update { it.copy(estimatedFee = fee) }
            }
        }
    }

    /**
     * Submit transaction with biometric confirmation.
     */
    fun onSendClick(activity: FragmentActivity) {
        viewModelScope.launch {
            val state = _sendState.value

            // Validate
            if (!state.isValid) {
                _events.emit(SendEvent.Error("Invalid send details"))
                return@launch
            }

            // Biometric auth
            _sendState.update { it.copy(isSubmitting = true) }

            val authResult = authenticateWithBiometricsUseCase(
                activity = activity,
                title = "Confirm Send",
                subtitle = "Send ${state.amount} ${state.selectedToken}"
            )

            if (authResult.isFailure) {
                _sendState.update { it.copy(isSubmitting = false) }
                _events.emit(SendEvent.Error("Authentication failed"))
                return@launch
            }

            // Send transaction
            val sendResult = sendTokenUseCase(
                recipient = state.recipient,
                tokenSymbol = state.selectedToken,
                amount = state.amount.toDouble()
            )

            sendResult.onSuccess { txHash ->
                _events.emit(SendEvent.Success(txHash))
                resetForm()
            }.onFailure { e ->
                _events.emit(SendEvent.Error(e.message ?: "Send failed"))
            }

            _sendState.update { it.copy(isSubmitting = false) }
        }
    }

    private fun resetForm() {
        _sendState.value = SendState()
    }
}

/**
 * Send form state.
 */
data class SendState(
    val selectedToken: String = "SOL",
    val amount: String = "",
    val amountUsd: Double = 0.0,
    val recipient: String = "",
    val recipientValid: Boolean = false,
    val maxBalance: Double = 0.0,
    val tokenPriceUsd: Double = 0.0,
    val estimatedFee: Double = 0.000005,
    val isSubmitting: Boolean = false
) {
    val isValid: Boolean
        get() = amount.toDoubleOrNull() != null
                && amount.toDouble() > 0
                && amount.toDouble() <= maxBalance
                && recipientValid
}

/**
 * Send events.
 */
sealed interface SendEvent {
    data class Success(val txHash: String) : SendEvent
    data class Error(val message: String) : SendEvent
}