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
import com.decagon.domain.usecase.swap.*
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

    // State
    private val _uiState = MutableStateFlow<SwapUiState>(SwapUiState.Idle)
    val uiState: StateFlow<SwapUiState> = _uiState.asStateFlow()

    private val _currentWallet = MutableStateFlow<com.decagon.domain.model.DecagonWallet?>(null)
    val currentWallet: StateFlow<com.decagon.domain.model.DecagonWallet?> = _currentWallet.asStateFlow()

    private val _inputToken = MutableStateFlow(CommonTokens.SOL)
    val inputToken: StateFlow<TokenInfo> = _inputToken.asStateFlow()

    private val _outputToken = MutableStateFlow(CommonTokens.USDC)
    val outputToken: StateFlow<TokenInfo> = _outputToken.asStateFlow()

    private val _inputAmount = MutableStateFlow("")
    val inputAmount: StateFlow<String> = _inputAmount.asStateFlow()

    private val _currentQuote = MutableStateFlow<SwapOrder?>(null)
    val currentQuote: StateFlow<SwapOrder?> = _currentQuote.asStateFlow()

    private val _slippageTolerance = MutableStateFlow(0.5)
    val slippageTolerance: StateFlow<Double> = _slippageTolerance.asStateFlow()

    private val _tokenBalances = MutableStateFlow<List<TokenBalance>>(emptyList())
    val tokenBalances: StateFlow<List<TokenBalance>> = _tokenBalances.asStateFlow()

    private val _tokensLoading = MutableStateFlow(true)
    val tokensLoading: StateFlow<Boolean> = _tokensLoading.asStateFlow()

    private val _commonTokens = MutableStateFlow<List<TokenInfo>>(CommonTokens.ALL)
    val commonTokens: StateFlow<List<TokenInfo>> = _commonTokens.asStateFlow()

    private val _availableTokens = MutableStateFlow<List<TokenInfo>>(emptyList())
    val availableTokens: StateFlow<List<TokenInfo>> = _availableTokens.asStateFlow()

    // ✅ NEW: Validation state for input
    private val _inputError = MutableStateFlow<String?>(null)
    val inputError: StateFlow<String?> = _inputError.asStateFlow()

    private var currentActivity: FragmentActivity? = null
    private var quoteRefreshJob: Job? = null

    init {
        Timber.d("SwapViewModel initialized")
        loadWallet()
        loadAvailableTokens()
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

                // ✅ Revalidate current input after balance update
                validateInputAmount(_inputAmount.value)
            }.onFailure { error ->
                Timber.e(error, "Failed to load balances")
            }
        }
    }

    private fun loadAvailableTokens() {
        viewModelScope.launch {
            _tokensLoading.value = true

            val result = searchTokensForSwapUseCase("")
            result.onSuccess { tokens ->
                _availableTokens.value = tokens
                Timber.i("Loaded ${tokens.size} available tokens")
            }.onFailure { error ->
                Timber.e(error, "Failed to load available tokens")
                _availableTokens.value = CommonTokens.ALL
            }

            _tokensLoading.value = false
        }
    }

    // ✅ ENHANCED: Validate before allowing input
    fun onInputAmountChanged(amount: String) {
        // Only allow valid number input
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _inputAmount.value = amount

            // Clear error immediately when typing
            _inputError.value = null

            // Validate and debounce quote
            if (validateInputAmount(amount)) {
                debounceQuoteRefresh()
            }
        }
    }

    // ✅ NEW: Comprehensive input validation
    private fun validateInputAmount(amountStr: String): Boolean {
        // Clear previous errors
        _inputError.value = null
        _uiState.value = SwapUiState.Idle

        // Empty input is valid (no error)
        if (amountStr.isBlank()) {
            _currentQuote.value = null
            return false
        }

        // Parse amount
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            _inputError.value = "Enter a valid amount"
            _currentQuote.value = null
            return false
        }

        // ✅ CRITICAL: Check balance BEFORE API call
        val balance = _tokenBalances.value
            .find { it.mint == _inputToken.value.address }
            ?.uiAmount ?: 0.0

        when {
            balance == 0.0 -> {
                _inputError.value = "You have no ${_inputToken.value.symbol} to swap"
                _currentQuote.value = null
                _uiState.value = SwapUiState.InsufficientBalance(
                    token = _inputToken.value.symbol,
                    required = amount,
                    available = 0.0
                )
                return false
            }

            amount > balance -> {
                _inputError.value = "Insufficient balance. Max: %.4f %s".format(
                    balance,
                    _inputToken.value.symbol
                )
                _currentQuote.value = null
                _uiState.value = SwapUiState.InsufficientBalance(
                    token = _inputToken.value.symbol,
                    required = amount,
                    available = balance
                )
                return false
            }

            // ✅ Minimum swap amount check (e.g., 0.0001 SOL)
            amount < 0.0001 && _inputToken.value.symbol == "SOL" -> {
                _inputError.value = "Minimum swap: 0.0001 SOL"
                _currentQuote.value = null
                return false
            }
        }

        return true
    }

    fun onInputTokenSelected(token: TokenInfo) {
        if (token.address == _outputToken.value.address) {
            val temp = _outputToken.value
            _outputToken.value = _inputToken.value
            _inputToken.value = temp
        } else {
            _inputToken.value = token
        }

        // ✅ Revalidate with new token
        validateInputAmount(_inputAmount.value)
        refreshQuote()
    }

    fun onOutputTokenSelected(token: TokenInfo) {
        if (token.address == _inputToken.value.address) {
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
        _inputError.value = null
        _currentQuote.value = null
        _uiState.value = SwapUiState.Idle
    }

    fun onSlippageToleranceChanged(tolerance: Double) {
        _slippageTolerance.value = tolerance
        refreshQuote()
    }

    fun refreshQuote() {
        val input = _inputToken.value
        val output = _outputToken.value
        val amountStr = _inputAmount.value

        // ✅ Pre-validate before API call
        if (!validateInputAmount(amountStr)) {
            return
        }

        val amount = amountStr.toDouble()
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
                    _currentQuote.value = null

                    // ✅ Enhanced error mapping (balance errors already caught above)
                    val errorMessage = when {
                        error.message?.contains("No trading route", ignoreCase = true) == true ||
                                error.message?.contains("No routes found", ignoreCase = true) == true ->
                            "No trading route available for ${input.symbol} → ${output.symbol}"

                        error.message?.contains("Insufficient liquidity", ignoreCase = true) == true ->
                            "Insufficient liquidity. Try a smaller amount or different token."

                        error.message?.contains("Invalid response", ignoreCase = true) == true ->
                            "This token pair may not be supported. Try different tokens."

                        error.message?.contains("Network error", ignoreCase = true) == true ||
                                error.message?.contains("Unable to connect", ignoreCase = true) == true ->
                            "Network error. Please check your connection."

                        else -> {
                            Timber.e(error, "Unexpected swap error: ${error.message}")
                            error.message ?: "Unable to get quote for this token pair"
                        }
                    }

                    _uiState.value = SwapUiState.Error(errorMessage)
                    Timber.w("Quote failed: ${input.symbol} → ${output.symbol}: $errorMessage")
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
                priorityFeeLamports = 0
            )

            result.fold(
                onSuccess = { signature ->
                    _uiState.value = SwapUiState.SwapSuccess(signature)
                    _currentQuote.value = null
                    _inputAmount.value = ""
                    _inputError.value = null
                    loadBalances()
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
            delay(500)
            refreshQuote()
        }
    }
}

// ✅ ENHANCED: New state for insufficient balance
sealed interface SwapUiState {
    data object Idle : SwapUiState
    data object LoadingQuote : SwapUiState
    data class QuoteReady(val quote: SwapOrder) : SwapUiState
    data class QuoteWithWarnings(val quote: SwapOrder) : SwapUiState
    data object ExecutingSwap : SwapUiState
    data class SwapSuccess(val signature: String) : SwapUiState
    data class Error(val message: String) : SwapUiState

    // ✅ NEW: Specific state for balance errors
    data class InsufficientBalance(
        val token: String,
        val required: Double,
        val available: Double
    ) : SwapUiState
}