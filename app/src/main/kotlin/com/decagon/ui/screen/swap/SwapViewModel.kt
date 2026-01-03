package com.decagon.ui.screen.swap

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.domain.model.CommonTokens
import com.decagon.domain.model.SwapOrder
import com.decagon.domain.model.TokenBalance
import com.decagon.domain.model.TokenInfo
import com.decagon.domain.model.WarningSeverity
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.usecase.swap.ExecuteSwapUseCase
import com.decagon.domain.usecase.swap.GetSwapHistoryUseCase
import com.decagon.domain.usecase.swap.GetSwapQuoteUseCase
import com.decagon.domain.usecase.swap.GetTokenBalancesUseCase
import com.decagon.domain.usecase.swap.SearchTokensForSwapUseCase
import com.decagon.domain.usecase.swap.ValidateTokenSecurityUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class SwapViewModel(
    private val getSwapQuoteUseCase: GetSwapQuoteUseCase,
    private val executeSwapUseCase: ExecuteSwapUseCase,
    private val searchTokensForSwapUseCase: SearchTokensForSwapUseCase,
    private val getTokenBalancesUseCase: GetTokenBalancesUseCase,
    private val validateSecurityUseCase: ValidateTokenSecurityUseCase,
    private val getSwapHistoryUseCase: GetSwapHistoryUseCase,
    private val walletRepository: DecagonWalletRepository
) : ViewModel() {

    // ✅ CRITICAL: All MutableStateFlow must be initialized BEFORE init block
    // State
    private val _uiState = MutableStateFlow<SwapUiState>(SwapUiState.Idle)
    val uiState: StateFlow<SwapUiState> = _uiState.asStateFlow()

    private val _currentWallet = MutableStateFlow<com.decagon.domain.model.DecagonWallet?>(null)
    val currentWallet: StateFlow<com.decagon.domain.model.DecagonWallet?> = _currentWallet.asStateFlow()

    // Token selection
    private val _inputToken = MutableStateFlow(CommonTokens.SOL)
    val inputToken: StateFlow<TokenInfo> = _inputToken.asStateFlow()

    private val _outputToken = MutableStateFlow(CommonTokens.USDC)
    val outputToken: StateFlow<TokenInfo> = _outputToken.asStateFlow()

    // Amount
    private val _inputAmount = MutableStateFlow("")
    val inputAmount: StateFlow<String> = _inputAmount.asStateFlow()

    // Quote
    private val _currentQuote = MutableStateFlow<SwapOrder?>(null)
    val currentQuote: StateFlow<SwapOrder?> = _currentQuote.asStateFlow()

    // Settings
    private val _slippageTolerance = MutableStateFlow(0.5) // 0.5% (Jupiter recommended)
    val slippageTolerance: StateFlow<Double> = _slippageTolerance.asStateFlow()

    // Balances
    private val _tokenBalances = MutableStateFlow<List<TokenBalance>>(emptyList())
    val tokenBalances: StateFlow<List<TokenBalance>> = _tokenBalances.asStateFlow()

    // ✅ Loading state for tokens - initialized with true
    private val _tokensLoading = MutableStateFlow(true)
    val tokensLoading: StateFlow<Boolean> = _tokensLoading.asStateFlow()

    // ✅ Common tokens (quick access) - initialized with default value
    private val _commonTokens = MutableStateFlow<List<TokenInfo>>(CommonTokens.ALL)
    val commonTokens: StateFlow<List<TokenInfo>> = _commonTokens.asStateFlow()

    private val _availableTokens = MutableStateFlow<List<TokenInfo>>(emptyList())
    val availableTokens: StateFlow<List<TokenInfo>> = _availableTokens.asStateFlow()

    // Activity reference for biometric auth
    private var currentActivity: FragmentActivity? = null

    // Debounce job
    private var quoteRefreshJob: Job? = null

    // ✅ Init block AFTER all field declarations
    init {
        Timber.d("SwapViewModel initialized")
        loadWallet()
        loadAvailableTokens() // Safe to call now - all fields initialized
    }

    fun setActivity(activity: FragmentActivity) {
        currentActivity = activity
    }

    private fun loadWallet() {
        viewModelScope.launch {
            walletRepository.getActiveWallet().collect { wallet ->
                _currentWallet.value = wallet
                wallet?.let { loadBalances() }
            }
        }
    }

    private fun loadBalances() {
        val publicKey = _currentWallet.value?.address ?: return

        viewModelScope.launch {
            val result = getTokenBalancesUseCase(publicKey)
            result.onSuccess { balances ->
                _tokenBalances.value = balances
                Timber.i("Loaded ${balances.size} token balances")
            }.onFailure { error ->
                Timber.e(error, "Failed to load balances")
            }
        }
    }

    // ✅ UPDATED: Load full token list with loading state
    private fun loadAvailableTokens() {
        viewModelScope.launch {
            _tokensLoading.value = true

            val result = searchTokensForSwapUseCase("")
            result.onSuccess { tokens ->
                _availableTokens.value = tokens
                Timber.i("Loaded ${tokens.size} available tokens")
            }.onFailure { error ->
                Timber.e(error, "Failed to load available tokens")
                // Fallback to common tokens if full list fails
                _availableTokens.value = CommonTokens.ALL
            }

            _tokensLoading.value = false
        }
    }

    fun onInputAmountChanged(amount: String) {
        // Validate input (numbers and decimal only)
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _inputAmount.value = amount
            debounceQuoteRefresh()
        }
    }

    fun onInputTokenSelected(token: TokenInfo) {
        if (token.address == _outputToken.value.address) {
            // Auto-flip if selecting same token
            val temp = _outputToken.value
            _outputToken.value = _inputToken.value
            _inputToken.value = temp
        } else {
            _inputToken.value = token
        }
        refreshQuote()
    }

    fun onOutputTokenSelected(token: TokenInfo) {
        if (token.address == _inputToken.value.address) {
            // Auto-flip if selecting same token
            val temp = _inputToken.value
            _inputToken.value = _outputToken.value
            _outputToken.value = temp
        } else {
            _outputToken.value = token
        }
        refreshQuote()
    }

    fun onSwapDirectionFlipped() {
        val temp = _inputToken.value
        _inputToken.value = _outputToken.value
        _outputToken.value = temp
        _inputAmount.value = ""
        _currentQuote.value = null
    }

    fun onSlippageToleranceChanged(tolerance: Double) {
        _slippageTolerance.value = tolerance
        refreshQuote()
    }

    fun refreshQuote() {
        val input = _inputToken.value
        val output = _outputToken.value
        val amountStr = _inputAmount.value

        if (amountStr.isBlank() || amountStr.toDoubleOrNull() == null) {
            _currentQuote.value = null
            _uiState.value = SwapUiState.Idle
            return
        }

        val amount = amountStr.toDouble()
        if (amount <= 0) {
            _currentQuote.value = null
            _uiState.value = SwapUiState.Idle
            return
        }

        val userPublicKey = _currentWallet.value?.address ?: run {
            _uiState.value = SwapUiState.Error("Wallet not connected")
            return
        }

        viewModelScope.launch {
            _uiState.value = SwapUiState.LoadingQuote

            val result = getSwapQuoteUseCase(
                inputToken = input,
                outputToken = output,
                inputAmount = amount,
                userPublicKey = userPublicKey,
                slippageTolerance = _slippageTolerance.value
            )

            result.fold(
                onSuccess = { quote ->
                    _currentQuote.value = quote

                    val hasWarnings = quote.securityWarnings.values
                        .flatten()
                        .any { it.severity != WarningSeverity.INFO }

                    _uiState.value = if (hasWarnings) {
                        SwapUiState.QuoteWithWarnings(quote)
                    } else {
                        SwapUiState.QuoteReady(quote)
                    }
                },
                onFailure = { error ->
                    _uiState.value = SwapUiState.Error(
                        error.message ?: "Failed to get quote"
                    )
                }
            )
        }
    }

    fun executeSwap() {
        val quote = _currentQuote.value ?: return
        val wallet = _currentWallet.value ?: return
        val activity = currentActivity ?: run {
            _uiState.value = SwapUiState.Error("Activity not available")
            return
        }

        viewModelScope.launch {
            _uiState.value = SwapUiState.ExecutingSwap

            val result = executeSwapUseCase(
                swapOrder = quote,
                walletId = wallet.id,
                inputToken = _inputToken.value,
                outputToken = _outputToken.value,
                activity = activity,
                priorityFeeLamports = 0 // TODO: Add priority fee UI
            )

            result.fold(
                onSuccess = { signature ->
                    _uiState.value = SwapUiState.SwapSuccess(signature)
                    _currentQuote.value = null
                    _inputAmount.value = ""
                    loadBalances() // Refresh balances
                },
                onFailure = { error ->
                    _uiState.value = SwapUiState.Error(
                        error.message ?: "Swap failed"
                    )
                }
            )
        }
    }

    private fun debounceQuoteRefresh() {
        quoteRefreshJob?.cancel()
        quoteRefreshJob = viewModelScope.launch {
            delay(500) // 500ms debounce
            refreshQuote()
        }
    }
}

sealed interface SwapUiState {
    data object Idle : SwapUiState
    data object LoadingQuote : SwapUiState
    data class QuoteReady(val quote: SwapOrder) : SwapUiState
    data class QuoteWithWarnings(val quote: SwapOrder) : SwapUiState
    data object ExecutingSwap : SwapUiState
    data class SwapSuccess(val signature: String) : SwapUiState
    data class Error(val message: String) : SwapUiState
}