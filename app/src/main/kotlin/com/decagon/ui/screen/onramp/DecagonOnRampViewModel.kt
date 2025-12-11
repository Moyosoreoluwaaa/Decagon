package com.decagon.ui.screen.onramp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.OnRampRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonOnRampViewModel(
    private val onRampRepository: OnRampRepository,
    private val rpcClient: SolanaRpcClient
) : ViewModel() {

    private val _onRampState = MutableStateFlow<OnRampState>(OnRampState.Idle)
    val onRampState: StateFlow<OnRampState> = _onRampState.asStateFlow()

    private var currentTransactionId: String? = null
    private var monitoringJob: kotlinx.coroutines.Job? = null

    init {
        Timber.d("DecagonOnRampViewModel initialized")
    }

    fun initializeOnRamp(
        wallet: DecagonWallet,
        cryptoAsset: String
    ) {
        viewModelScope.launch {
            Timber.d("Initializing on-ramp for wallet: ${wallet.id}")

            val activeChain = wallet.activeChain
            if (activeChain == null) {
                _onRampState.value = OnRampState.Error("No active chain selected")
                return@launch
            }

            // Create transaction record
            val result = onRampRepository.createOnRampTransaction(
                walletId = wallet.id,
                walletAddress = activeChain.address,
                chainId = activeChain.chainId,
                fiatAmount = 10000.0, // Default ₦10,000
                fiatCurrency = "NGN",
                cryptoAsset = cryptoAsset,
                provider = "ramp"
            )

            result.fold(
                onSuccess = { txId ->
                    currentTransactionId = txId
                    _onRampState.value = OnRampState.Initialized(txId)
                    Timber.i("On-ramp transaction initialized: $txId")
                },
                onFailure = { error ->
                    _onRampState.value = OnRampState.Error(error.message ?: "Failed to initialize")
                    Timber.e(error, "Failed to initialize on-ramp")
                }
            )
        }
    }

    fun startMonitoring(walletAddress: String) {
        val txId = currentTransactionId ?: return

        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            Timber.d("Starting balance monitoring for on-ramp: $txId")

            var previousBalance = 0L

            // Get initial balance
            rpcClient.getBalance(walletAddress).getOrNull()?.let { balance ->
                previousBalance = balance
                Timber.d("Initial balance: $previousBalance lamports")
            }

            // Poll every 5 seconds for 10 minutes
            repeat(120) { attempt ->
                delay(5000)

                val currentBalance = rpcClient.getBalance(walletAddress).getOrNull() ?: 0L

                if (currentBalance > previousBalance) {
                    val difference = currentBalance - previousBalance
                    val solAmount = difference / 1_000_000_000.0

                    Timber.i("Balance increased! Received $solAmount SOL")

                    // Mark transaction as completed
                    onRampRepository.markTransactionCompleted(
                        txId = txId,
                        signature = "onramp_$txId", // Placeholder signature
                        actualAmount = solAmount
                    )

                    _onRampState.value = OnRampState.Completed(solAmount)
                    return@launch
                }

                if (attempt % 6 == 0) { // Log every 30 seconds
                    Timber.d("Still monitoring... attempt ${attempt + 1}/120")
                }
            }

            // Timeout after 10 minutes
            Timber.w("Monitoring timeout for on-ramp: $txId")
            _onRampState.value = OnRampState.Error("Transaction monitoring timeout")
        }
    }

    fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
        Timber.d("Stopped on-ramp monitoring")
    }

    override fun onCleared() {
        super.onCleared()
        stopMonitoring()
    }
}

sealed interface OnRampState {
    data object Idle : OnRampState
    data class Initialized(val txId: String) : OnRampState
    data class Completed(val amount: Double) : OnRampState
    data class Error(val message: String) : OnRampState
}