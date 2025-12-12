// ============================================================================
// FILE: ui/screen/onramp/DecagonOnRampViewModel.kt
// UPDATED: Manual provider selection support
// ============================================================================

package com.decagon.ui.screen.onramp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.config.OnRampConfig
import com.decagon.core.config.OnRampProviderType
import com.decagon.data.provider.ProviderInfoHelper
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.provider.OnRampProviderFactory
import com.decagon.domain.repository.OnRampRepository
import com.decagon.ui.components.ProviderInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class DecagonOnRampViewModel(
    private val onRampRepository: OnRampRepository,
    private val rpcClient: SolanaRpcClient,
    private val providerFactory: OnRampProviderFactory
) : ViewModel() {

    private val _onRampState = MutableStateFlow<OnRampState>(OnRampState.Idle)
    val onRampState: StateFlow<OnRampState> = _onRampState.asStateFlow()

    private val _showProviderSelection = MutableStateFlow(false)
    val showProviderSelection: StateFlow<Boolean> = _showProviderSelection.asStateFlow()

    private val _selectedProvider = MutableStateFlow<OnRampProviderType?>(null)
    val selectedProvider: StateFlow<OnRampProviderType?> = _selectedProvider.asStateFlow()

    private var currentTransactionId: String? = null
    private var monitoringJob: kotlinx.coroutines.Job? = null
    private var currentWallet: DecagonWallet? = null
    private var currentCryptoAsset: String? = null

    init {
        Timber.d("DecagonOnRampViewModel initialized with multi-provider support")
    }

    /**
     * Start on-ramp flow.
     * Shows provider selection if enabled, otherwise uses automatic selection.
     */
    fun startOnRampFlow(
        wallet: DecagonWallet,
        cryptoAsset: String
    ) {
        currentWallet = wallet
        currentCryptoAsset = cryptoAsset

        if (OnRampConfig.ALLOW_MANUAL_PROVIDER_SELECTION) {
            // Show provider selection UI
            _showProviderSelection.value = true
            _onRampState.value = OnRampState.SelectingProvider
        } else {
            // Use automatic provider selection
            initializeOnRamp(wallet, cryptoAsset, preferredProvider = null)
        }
    }

    /**
     * User selected a provider manually.
     */
    fun onProviderSelected(provider: OnRampProviderType) {
        _selectedProvider.value = provider
        _showProviderSelection.value = false

        val wallet = currentWallet ?: return
        val asset = currentCryptoAsset ?: return

        initializeOnRamp(wallet, asset, preferredProvider = provider)
    }

    /**
     * Get available providers for user's region.
     */
    fun getAvailableProviders(): List<ProviderInfo> {
        val userCountry = ProviderInfoHelper.detectUserCountryCode()
        return ProviderInfoHelper.getProvidersForDisplay(
            factory = providerFactory,
            userCountryCode = userCountry
        )
    }

    /**
     * Initialize on-ramp with selected or automatic provider.
     */
    private fun initializeOnRamp(
        wallet: DecagonWallet,
        cryptoAsset: String,
        preferredProvider: OnRampProviderType?
    ) {
        viewModelScope.launch {
            Timber.d("Initializing on-ramp for wallet: ${wallet.id}")
            _onRampState.value = OnRampState.Loading

            val activeChain = wallet.activeChain
            if (activeChain == null) {
                _onRampState.value = OnRampState.Error("No active chain selected")
                return@launch
            }

            // Get provider (respecting manual selection)
            val providerResult = providerFactory.getProvider(preferredProvider)
            if (providerResult.isFailure) {
                val error = providerResult.exceptionOrNull()?.message
                    ?: "No providers available"
                _onRampState.value = OnRampState.Error(error)
                Timber.e("Failed to get provider: $error")
                return@launch
            }

            val provider = providerResult.getOrThrow()
            Timber.i("Selected provider: ${provider.getDisplayName()}")

            // Check regional availability
            val userCountry = ProviderInfoHelper.detectUserCountryCode()
            val isAvailable = OnRampConfig.isProviderAvailableInRegion(
                provider.providerType,
                userCountry
            )

            if (!isAvailable) {
                val message = OnRampConfig.getRegionalAvailabilityMessage(
                    provider.providerType,
                    userCountry
                )
                _onRampState.value = OnRampState.Error(message)
                Timber.e("Provider not available in region: $message")
                return@launch
            }

            // Build widget URL
            val urlResult = provider.buildWidgetUrl(
                wallet = wallet,
                cryptoAsset = cryptoAsset,
                fiatAmount = 10000.0,
                fiatCurrency = "NGN",
                isTestMode = OnRampConfig.TEST_MODE
            )

            if (urlResult.isFailure) {
                val error = urlResult.exceptionOrNull()?.message
                    ?: "Failed to build widget URL"
                _onRampState.value = OnRampState.Error(error)
                Timber.e("URL building failed: $error")
                return@launch
            }

            val widgetUrl = urlResult.getOrThrow()

            // Create transaction record
            val txResult = onRampRepository.createOnRampTransaction(
                walletId = wallet.id,
                walletAddress = activeChain.address,
                chainId = activeChain.chainId,
                fiatAmount = 10000.0,
                fiatCurrency = "NGN",
                cryptoAsset = cryptoAsset,
                provider = provider.providerType.name.lowercase()
            )

            txResult.fold(
                onSuccess = { txId ->
                    currentTransactionId = txId
                    _onRampState.value = OnRampState.Ready(
                        widgetUrl = widgetUrl,
                        provider = provider.getDisplayName(),
                        transactionId = txId
                    )
                    Timber.i("On-ramp initialized: $txId via ${provider.getDisplayName()}")
                },
                onFailure = { error ->
                    _onRampState.value = OnRampState.Error(
                        error.message ?: "Failed to initialize"
                    )
                    Timber.e(error, "Failed to create transaction")
                }
            )
        }
    }

    /**
     * Cancel provider selection and return to wallet.
     */
    fun cancelProviderSelection() {
        _showProviderSelection.value = false
        _onRampState.value = OnRampState.Idle
    }

    /**
     * Start monitoring blockchain for balance changes.
     */
    fun startMonitoring(walletAddress: String) {
        val txId = currentTransactionId ?: return

        monitoringJob?.cancel()
        monitoringJob = viewModelScope.launch {
            Timber.d("Starting balance monitoring for on-ramp: $txId")

            var previousBalance = 0L

            rpcClient.getBalance(walletAddress).getOrNull()?.let { balance ->
                previousBalance = balance
                Timber.d("Initial balance: $previousBalance lamports")
            }

            val maxAttempts = (OnRampConfig.MONITORING_DURATION_MINUTES * 60) /
                    OnRampConfig.POLLING_INTERVAL_SECONDS

            repeat(maxAttempts.toInt()) { attempt ->
                delay(OnRampConfig.POLLING_INTERVAL_SECONDS * 1000)

                val currentBalance = rpcClient.getBalance(walletAddress).getOrNull() ?: 0L

                if (currentBalance > previousBalance) {
                    val difference = currentBalance - previousBalance
                    val solAmount = difference / 1_000_000_000.0

                    Timber.i("Balance increased! Received $solAmount SOL")

                    onRampRepository.markTransactionCompleted(
                        txId = txId,
                        signature = "onramp_$txId",
                        actualAmount = solAmount
                    )

                    _onRampState.value = OnRampState.Completed(solAmount)
                    return@launch
                }

                if (attempt % 6 == 0) {
                    Timber.d("Still monitoring... attempt ${attempt + 1}/$maxAttempts")
                }
            }

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

/**
 * On-ramp UI state.
 */
sealed interface OnRampState {
    data object Idle : OnRampState
    data object SelectingProvider : OnRampState
    data object Loading : OnRampState

    data class Ready(
        val widgetUrl: String,
        val provider: String,
        val transactionId: String
    ) : OnRampState

    data class Completed(val amount: Double) : OnRampState
    data class Error(val message: String) : OnRampState
}