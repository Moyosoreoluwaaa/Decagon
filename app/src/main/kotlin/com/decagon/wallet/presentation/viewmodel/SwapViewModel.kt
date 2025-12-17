package com.octane.wallet.presentation.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.wallet.core.blockchain.JupiterApiService
import com.octane.wallet.core.blockchain.JupiterQuoteResponse
import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.usecases.transaction.EstimateTransactionFeeUseCase
import com.octane.wallet.domain.usecases.transaction.SwapTokensUseCase
import com.octane.wallet.domain.usecases.security.AuthenticateWithBiometricsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Swap screen ViewModel.
 * Handles token swapping via Jupiter Aggregator.
 * 
 * RESPONSIBILITY:
 * - Token selection and validation
 * - Real-time rate updates (every 5s)
 * - Slippage configuration
 * - Route visualization
 * - Price impact warnings
 * - MEV protection toggle
 * 
 * Pattern: features/swap/SwapViewModel.kt
 */
class SwapViewModel(
    private val swapTokensUseCase: SwapTokensUseCase,
    private val estimateFeeUseCase: EstimateTransactionFeeUseCase,
    private val authenticateWithBiometricsUseCase: AuthenticateWithBiometricsUseCase,
    private val jupiterApi: JupiterApiService,
    private val basePortfolio: BasePortfolioViewModel,
    private val baseWallet: BaseWalletViewModel
) : ViewModel() {
    
    // UI State: Swap form
    private val _swapState = MutableStateFlow(SwapState())
    val swapState: StateFlow<SwapState> = _swapState.asStateFlow()
    
    // Delegate: Portfolio (for token list)
    val portfolioState = basePortfolio.portfolioState
    
    // Delegate: Active wallet
    val activeWallet = baseWallet.activeWallet
    
    // One-time events
    private val _events = MutableSharedFlow<SwapEvent>(replay = 0)
    val events: SharedFlow<SwapEvent> = _events.asSharedFlow()
    
    // Real-time rate updates
    private var rateUpdateJob: Job? = null
    
    init {
        // Default: SOL → USDC
        selectFromToken("SOL")
        selectToToken("USDC")
    }
    
    /**
     * Update "You pay" amount.
     * Triggers rate refresh and output amount calculation.
     */
    fun onFromAmountChanged(amount: String) {
        val parsed = amount.toDoubleOrNull() ?: 0.0
        _swapState.update { it.copy(fromAmount = amount) }
        
        // Debounce rate updates (wait 300ms after typing stops)
        rateUpdateJob?.cancel()
        rateUpdateJob = viewModelScope.launch {
            delay(300)
            if (parsed > 0) {
                fetchQuote()
            } else {
                _swapState.update { it.copy(toAmount = "", rate = null, priceImpact = null) }
            }
        }
    }
    
    /**
     * Select "You pay" token.
     * Updates max balance and triggers rate refresh.
     */
    fun selectFromToken(tokenSymbol: String) {
        val asset = (portfolioState.value as? LoadingState.Success)
            ?.data?.assets?.find { it.symbol == tokenSymbol }
        
        if (asset != null) {
            _swapState.update {
                it.copy(
                    fromToken = tokenSymbol,
                    fromTokenBalance = asset.balance.toDouble(),
                    fromTokenPriceUsd = asset.priceUsd ?: 0.0
                )
            }
            
            // Re-fetch quote if amount exists
            if (_swapState.value.fromAmount.toDoubleOrNull() != null) {
                fetchQuote()
            }
        }
    }
    
    /**
     * Select "You receive" token.
     */
    fun selectToToken(tokenSymbol: String) {
        val asset = (portfolioState.value as? LoadingState.Success)
            ?.data?.assets?.find { it.symbol == tokenSymbol }
        
        if (asset != null) {
            _swapState.update {
                it.copy(
                    toToken = tokenSymbol,
                    toTokenPriceUsd = asset.priceUsd ?: 0.0
                )
            }
            
            // Re-fetch quote
            if (_swapState.value.fromAmount.toDoubleOrNull() != null) {
                fetchQuote()
            }
        }
    }
    
    /**
     * Reverse token pair (swap button).
     */
    fun reverseTokens() {
        val currentFrom = _swapState.value.fromToken
        val currentTo = _swapState.value.toToken
        
        selectFromToken(currentTo)
        selectToToken(currentFrom)
        
        // Swap amounts if output amount exists
        val outputAmount = _swapState.value.toAmount
        if (outputAmount.isNotBlank()) {
            onFromAmountChanged(outputAmount)
        }
    }
    
    /**
     * Quick amount buttons (25%, 50%, 75%, Max).
     */
    fun onQuickAmountClick(percentage: Float) {
        val amount = _swapState.value.fromTokenBalance * percentage
        onFromAmountChanged(amount.toString())
    }
    
    /**
     * Update slippage tolerance (basis points).
     * 100 bps = 1%
     */
    fun onSlippageChanged(bps: Int) {
        _swapState.update { it.copy(slippageBps = bps) }
        
        // Re-fetch quote with new slippage
        if (_swapState.value.fromAmount.toDoubleOrNull() != null) {
            fetchQuote()
        }
    }
    
    /**
     * Toggle MEV protection (Jito bundles).
     */
    fun toggleMevProtection(enabled: Boolean) {
        _swapState.update { it.copy(mevProtectionEnabled = enabled) }
    }
    
    /**
     * Fetch quote from Jupiter.
     * Updates rate, output amount, routes, price impact.
     */
    private fun fetchQuote() {
        viewModelScope.launch {
            try {
                val state = _swapState.value
                val amount = state.fromAmount.toDoubleOrNull() ?: return@launch
                
                _swapState.update { it.copy(isFetchingQuote = true) }
                
                // Get token mint addresses
                val inputMint = getTokenMintAddress(state.fromToken)
                val outputMint = getTokenMintAddress(state.toToken)
                
                // Convert to lamports
                val amountLamports = (amount * 1_000_000_000).toLong()
                
                // Get quote
                val quote = jupiterApi.getQuote(
                    inputMint = inputMint,
                    outputMint = outputMint,
                    amount = amountLamports,
                    slippageBps = state.slippageBps
                )
                
                // Calculate output amount
                val outputAmount = quote.outAmount.toLong() / 1_000_000_000.0
                
                // Calculate rate (1 FROM = X TO)
                val rate = outputAmount / amount
                
                // Extract route info
                val routes = quote.routePlan.map { plan ->
                    SwapRoute(
                        dex = plan.swapInfo.label,
                        percentage = plan.percent
                    )
                }
                
                _swapState.update {
                    it.copy(
                        toAmount = outputAmount.toString(),
                        rate = rate,
                        priceImpact = quote.priceImpactPct,
                        routes = routes,
                        jupiterQuote = quote,
                        isFetchingQuote = false
                    )
                }
                
                // Estimate fee
                estimateFee()
                
            } catch (e: Exception) {
                _swapState.update { it.copy(isFetchingQuote = false) }
                _events.emit(SwapEvent.Error("Failed to fetch quote: ${e.message}"))
            }
        }
    }
    
    /**
     * Estimate transaction fee.
     */
    private fun estimateFee() {
        viewModelScope.launch {
            val state = _swapState.value
            val feeResult = estimateFeeUseCase(
                tokenSymbol = state.fromToken,
                amount = state.fromAmount.toDouble()
            )
            
            feeResult.onSuccess { fee ->
                _swapState.update { it.copy(estimatedFee = fee) }
            }
        }
    }
    
    /**
     * Execute swap with biometric confirmation.
     */
    fun onSwapClick(activity: FragmentActivity) {
        viewModelScope.launch {
            val state = _swapState.value
            
            // Validate
            if (!state.isValid) {
                _events.emit(SwapEvent.Error("Invalid swap details"))
                return@launch
            }
            
            // Warn on high price impact (>5%)
            if ((state.priceImpact ?: 0.0) > 5.0) {
                _events.emit(SwapEvent.HighPriceImpactWarning(state.priceImpact!!))
                return@launch
            }
            
            // Biometric auth
            _swapState.update { it.copy(isSubmitting = true) }
            
            val authResult = authenticateWithBiometricsUseCase(
                activity = activity,
                title = "Confirm Swap",
                subtitle = "Swap ${state.fromAmount} ${state.fromToken} → ${state.toAmount} ${state.toToken}"
            )
            
            if (authResult.isFailure) {
                _swapState.update { it.copy(isSubmitting = false) }
                _events.emit(SwapEvent.Error("Authentication failed"))
                return@launch
            }
            
            // Execute swap
            val swapResult = swapTokensUseCase(
                fromToken = state.fromToken,
                toToken = state.toToken,
                amount = state.fromAmount.toDouble(),
                slippageBps = state.slippageBps
            )
            
            swapResult.onSuccess { txHash ->
                _events.emit(SwapEvent.Success(txHash))
                resetForm()
            }.onFailure { e ->
                _events.emit(SwapEvent.Error(e.message ?: "Swap failed"))
            }
            
            _swapState.update { it.copy(isSubmitting = false) }
        }
    }
    
    /**
     * Confirm swap after high price impact warning.
     */
    fun confirmHighImpactSwap(activity: FragmentActivity) {
        viewModelScope.launch {
            _swapState.update { it.copy(highImpactConfirmed = true) }
            onSwapClick(activity)
        }
    }
    
    private fun resetForm() {
        _swapState.value = SwapState(
            fromToken = "SOL",
            toToken = "USDC"
        )
        selectFromToken("SOL")
        selectToToken("USDC")
    }
    
    private fun getTokenMintAddress(symbol: String): String {
        return when (symbol) {
            "SOL" -> "So11111111111111111111111111111111111111112"
            "USDC" -> "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
            "USDT" -> "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"
            "BONK" -> "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"
            else -> throw IllegalArgumentException("Unsupported token: $symbol")
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        rateUpdateJob?.cancel()
    }
}

/**
 * Swap form state.
 */
data class SwapState(
    val fromToken: String = "SOL",
    val toToken: String = "USDC",
    val fromAmount: String = "",
    val toAmount: String = "",
    val fromTokenBalance: Double = 0.0,
    val fromTokenPriceUsd: Double = 0.0,
    val toTokenPriceUsd: Double = 0.0,
    val rate: Double? = null, // 1 FROM = X TO
    val priceImpact: Double? = null, // Percentage
    val routes: List<SwapRoute> = emptyList(),
    val slippageBps: Int = 100, // 1%
    val mevProtectionEnabled: Boolean = false,
    val estimatedFee: Double = 0.000005,
    val isFetchingQuote: Boolean = false,
    val isSubmitting: Boolean = false,
    val highImpactConfirmed: Boolean = false,
    val jupiterQuote: JupiterQuoteResponse? = null
) {
    val isValid: Boolean
        get() = fromAmount.toDoubleOrNull() != null
            && fromAmount.toDouble() > 0
            && fromAmount.toDouble() <= fromTokenBalance
            && toAmount.isNotBlank()
            && jupiterQuote != null
    
    val priceImpactColor: Color
        get() = when {
            priceImpact == null -> Color.Gray
            priceImpact < 1.0 -> Color(0xFF4ECDC4) // Teal (good)
            priceImpact < 5.0 -> Color(0xFFF7DC6F) // Yellow (warning)
            else -> Color(0xFFFF6B6B) // Red (danger)
        }
}

data class SwapRoute(
    val dex: String, // "Raydium", "Orca", etc.
    val percentage: Int // Percentage of route
)

/**
 * Swap events.
 */
sealed interface SwapEvent {
    data class Success(val txHash: String) : SwapEvent
    data class Error(val message: String) : SwapEvent
    data class HighPriceImpactWarning(val impact: Double) : SwapEvent
}