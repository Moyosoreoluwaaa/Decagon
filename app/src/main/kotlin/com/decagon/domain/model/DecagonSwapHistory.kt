package com.decagon.domain.model

/**
 * Decagon Swap History Domain Model
 * 
 * Represents a completed or pending swap in the user's wallet.
 * This is the domain layer representation - pure Kotlin with
 * no Android or database dependencies.
 */
data class DecagonSwapHistory(
    val id: String,
    val walletAddress: String,
    val inputMint: String,
    val outputMint: String,
    val inputAmount: Double,
    val outputAmount: Double,
    val inputSymbol: String,
    val outputSymbol: String,
    val signature: String?,
    val status: DecagonSwapStatus,
    val timestamp: Long,
    val priceImpact: Double,
    val routePlan: String?,
    val slippageBps: Int
)

/**
 * Swap execution status
 */
enum class DecagonSwapStatus {
    PENDING,    // Swap initiated, waiting for confirmation
    CONFIRMED,  // Swap confirmed on-chain
    FAILED      // Swap failed (rejected, timeout, or error)
}

/**
 * Swap quote summary for UI display
 * 
 * Simplified quote data for presentation layer.
 * Contains only what the UI needs to show.
 */
data class DecagonSwapQuoteSummary(
    val inputAmount: Double,
    val outputAmount: Double,
    val inputSymbol: String,
    val outputSymbol: String,
    val priceImpact: Double,
    val minimumOutput: Double,
    val exchangeRate: Double,
    val routeDescription: String,
    val estimatedFee: Long
)

/**
 * Token pair for swap
 * 
 * Represents the input/output token combination.
 * Used for swap direction toggling.
 */
data class DecagonSwapTokenPair(
    val inputToken: DecagonTokenMetadata,
    val outputToken: DecagonTokenMetadata
) {
    /**
     * Swaps input and output tokens
     */
    fun reverse(): DecagonSwapTokenPair {
        return DecagonSwapTokenPair(
            inputToken = outputToken,
            outputToken = inputToken
        )
    }
}

/**
 * Token metadata for swap UI
 * 
 * Simplified token info for domain layer.
 * Maps to DecagonTokenInfo from data layer.
 */
data class DecagonTokenMetadata(
    val mint: String,
    val symbol: String,
    val name: String,
    val decimals: Int,
    val logoUri: String?,
    val isVerified: Boolean = false,
    val isStablecoin: Boolean = false
)

/**
 * Swap validation result
 * 
 * Used to validate swap parameters before execution.
 */
sealed interface DecagonSwapValidation {
    data object Valid : DecagonSwapValidation
    
    sealed class Invalid(open val reason: String) : DecagonSwapValidation {
        data class InsufficientBalance(
            override val reason: String = "Insufficient balance for swap"
        ) : Invalid(reason)
        
        data class AmountTooSmall(
            val minimumAmount: Double,
            override val reason: String = "Amount must be at least $minimumAmount"
        ) : Invalid(reason)
        
        data class AmountTooLarge(
            val maximumAmount: Double,
            override val reason: String = "Amount exceeds maximum of $maximumAmount"
        ) : Invalid(reason)
        
        data class PriceImpactTooHigh(
            val priceImpact: Double,
            override val reason: String = "Price impact of $priceImpact% is too high"
        ) : Invalid(reason)
        
        data class SlippageTooHigh(
            val slippage: Double,
            override val reason: String = "Slippage of $slippage% is too high"
        ) : Invalid(reason)
        
        data class SameToken(
            override val reason: String = "Cannot swap token to itself"
        ) : Invalid(reason)
        
        data class NetworkError(
            override val reason: String = "Network unavailable"
        ) : Invalid(reason)
        
        data class Unknown(
            override val reason: String = "Unknown validation error"
        ) : Invalid(reason)
    }
}

/**
 * Swap execution result
 * 
 * Contains the outcome of a swap attempt.
 */
sealed interface DecagonSwapResult {
    data class Success(
        val signature: String,
        val swapId: String
    ) : DecagonSwapResult
    
    data class Failed(
        val error: Throwable,
        val message: String
    ) : DecagonSwapResult
    
    data object Cancelled : DecagonSwapResult
}

/**
 * Swap price comparison
 * 
 * Compares input and output prices for rate display.
 */
data class DecagonSwapPriceComparison(
    val inputToOutput: Double,  // How many output tokens per 1 input token
    val outputToInput: Double   // How many input tokens per 1 output token
) {
    /**
     * Formats price for display
     * 
     * Example:
     * - 1 SOL = 100 USDC
     * - 1 USDC = 0.01 SOL
     */
    fun formatPrice(inputSymbol: String, outputSymbol: String): String {
        return "1 $inputSymbol = ${"%.6f".format(inputToOutput)} $outputSymbol"
    }
    
    fun formatInversePrice(inputSymbol: String, outputSymbol: String): String {
        return "1 $outputSymbol = ${"%.6f".format(outputToInput)} $inputSymbol"
    }
}

/**
 * Slippage tolerance settings
 */
data class DecagonSlippageTolerance(
    val bps: Int,  // Basis points (50 = 0.5%)
    val percentage: Double = bps / 100.0
) {
    companion object {
        val LOW = DecagonSlippageTolerance(bps = 10)      // 0.1%
        val MEDIUM = DecagonSlippageTolerance(bps = 50)   // 0.5%
        val HIGH = DecagonSlippageTolerance(bps = 100)    // 1.0%
        val VERY_HIGH = DecagonSlippageTolerance(bps = 300) // 3.0%
        
        fun fromPercentage(percentage: Double): DecagonSlippageTolerance {
            return DecagonSlippageTolerance(bps = (percentage * 100).toInt())
        }
    }
}