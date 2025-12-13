package com.decagon.data.remote

import com.decagon.data.remote.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Jupiter Aggregator Swap Service for Decagon Wallet
 * 
 * Jupiter is Solana's largest DEX aggregator, routing swaps
 * across multiple liquidity sources (Raydium, Orca, etc.)
 * to find the best prices.
 * 
 * API Version: V6 (Latest as of 2025)
 * Official Docs: https://station.jup.ag/docs/apis/swap-api
 * 
 * Features:
 * - Real-time quote fetching
 * - Multi-hop routing optimization
 * - Slippage protection
 * - Price impact calculation
 * - Transaction building
 * 
 * @param httpClient Ktor HTTP client (from Koin)
 * @param baseUrl Jupiter API endpoint (mainnet/devnet)
 */
class DecagonJupiterSwapService(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://quote-api.jup.ag/v6"
) {
    
    init {
        Timber.d("DecagonJupiterSwapService initialized with URL: $baseUrl")
    }
    
    /**
     * Fetches swap quote from Jupiter Aggregator
     * 
     * This is Step 1 of the swap flow. The quote contains:
     * - Expected output amount
     * - Routing information (which DEXs to use)
     * - Price impact
     * - Minimum output amount (after slippage)
     * 
     * @param inputMint Token to sell (mint address)
     * @param outputMint Token to buy (mint address)
     * @param amount Input amount in smallest units (lamports/tokens)
     * @param slippageBps Slippage tolerance in basis points (50 = 0.5%)
     * @param swapMode "ExactIn" (default) or "ExactOut"
     * @param onlyDirectRoutes Only use direct swaps (no intermediate hops)
     * @param asLegacyTransaction Use legacy transaction format (for older wallets)
     * @param maxAccounts Limit transaction size by restricting accounts
     * 
     * @return Result containing quote or error
     * 
     * Example:
     * ```
     * val quote = jupiterService.getDecagonSwapQuote(
     *     inputMint = "So11111111111111111111111111111111111111112", // SOL
     *     outputMint = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", // USDC
     *     amount = 1_000_000_000, // 1 SOL
     *     slippageBps = 50 // 0.5% slippage
     * )
     * ```
     */
    suspend fun getDecagonSwapQuote(
        inputMint: String,
        outputMint: String,
        amount: Long,
        slippageBps: Int = 50,
        swapMode: String = "ExactIn",
        onlyDirectRoutes: Boolean = false,
        asLegacyTransaction: Boolean = false,
        maxAccounts: Int? = null
    ): Result<DecagonSwapQuoteResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("Fetching Decagon swap quote:")
            Timber.d("  Input: ${inputMint.take(8)}...")
            Timber.d("  Output: ${outputMint.take(8)}...")
            Timber.d("  Amount: $amount")
            Timber.d("  Slippage: ${slippageBps / 100.0}%")
            
            val response: DecagonSwapQuoteResponse = httpClient.get("$baseUrl/quote") {
                parameter("inputMint", inputMint)
                parameter("outputMint", outputMint)
                parameter("amount", amount)
                parameter("slippageBps", slippageBps)
                parameter("swapMode", swapMode)
                parameter("onlyDirectRoutes", onlyDirectRoutes)
                parameter("asLegacyTransaction", asLegacyTransaction)
                maxAccounts?.let { parameter("maxAccounts", it) }
            }.body()
            
            Timber.i("✅ Decagon quote received:")
            Timber.i("  Expected output: ${response.outAmount}")
            Timber.i("  Price impact: ${response.priceImpactPct}%")
            Timber.i("  Route steps: ${response.routePlan.size}")
            
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch Decagon swap quote")
            Result.failure(e)
        }
    }
    
    /**
     * Builds serialized swap transaction from quote
     * 
     * This is Step 2 of the swap flow. Returns a serialized transaction
     * that must be:
     * 1. Deserialized
     * 2. Signed with user's private key
     * 3. Submitted to Solana RPC
     * 
     * @param userPublicKey User's Solana wallet address
     * @param quoteResponse Quote from getDecagonSwapQuote()
     * @param priorityFeeLamports Priority fee (0 = auto, >0 = custom)
     * @param computeUnitPriceMicroLamports Compute budget price
     * @param wrapAndUnwrapSol Auto-wrap/unwrap SOL to WSOL (recommended)
     * @param useSharedAccounts Use shared accounts for efficiency (recommended)
     * @param feeAccount Referral fee account (optional)
     * 
     * @return Result containing serialized transaction or error
     * 
     * Example:
     * ```
     * val txResult = jupiterService.getDecagonSwapTransaction(
     *     userPublicKey = wallet.address,
     *     quoteResponse = quote,
     *     priorityFeeLamports = 5000
     * )
     * ```
     */
    suspend fun getDecagonSwapTransaction(
        userPublicKey: String,
        quoteResponse: DecagonSwapQuoteResponse,
        priorityFeeLamports: Long = 0,
        computeUnitPriceMicroLamports: Long? = null,
        wrapAndUnwrapSol: Boolean = true,
        useSharedAccounts: Boolean = true,
        feeAccount: String? = null
    ): Result<DecagonSwapTransactionResponse> = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("Building Decagon swap transaction for: ${userPublicKey.take(8)}...")
            
            val response: DecagonSwapTransactionResponse = httpClient.post("$baseUrl/swap") {
                contentType(ContentType.Application.Json)
                setBody(DecagonSwapTransactionRequest(
                    userPublicKey = userPublicKey,
                    quoteResponse = quoteResponse,
                    wrapAndUnwrapSol = wrapAndUnwrapSol,
                    useSharedAccounts = useSharedAccounts,
                    prioritizationFeeLamports = if (priorityFeeLamports > 0) {
                        priorityFeeLamports.toString()
                    } else null,
                    computeUnitPriceMicroLamports = computeUnitPriceMicroLamports?.toString(),
                    feeAccount = feeAccount
                ))
            }.body()
            
            Timber.i("✅ Decagon swap transaction built successfully")
            Timber.i("  Last valid block height: ${response.lastValidBlockHeight}")
            Timber.i("  Transaction size: ${response.swapTransaction.length} bytes (Base64)")
            
            Result.success(response)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to build Decagon swap transaction")
            Result.failure(e)
        }
    }
    
    /**
     * Fetches list of supported tokens
     * 
     * Returns tokens from Jupiter's curated token list.
     * 
     * @param verified Only return verified tokens (recommended for production)
     * 
     * @return Result containing list of tokens or error
     * 
     * Token list URLs:
     * - Strict (verified only): https://token.jup.ag/strict
     * - All tokens: https://token.jup.ag/all
     * 
     * Example:
     * ```
     * val tokens = jupiterService.getDecagonTokenList(verified = true)
     * // Use for token selector UI
     * ```
     */
    suspend fun getDecagonTokenList(
        verified: Boolean = true
    ): Result<List<DecagonTokenInfo>> = withContext(Dispatchers.IO) {
        return@withContext try {
            Timber.d("Fetching Decagon token list (verified: $verified)")
            
            val url = if (verified) {
                "https://token.jup.ag/strict"
            } else {
                "https://token.jup.ag/all"
            }
            
            val tokens: List<DecagonTokenInfo> = httpClient.get(url).body()
            
            Timber.i("✅ Fetched ${tokens.size} Decagon tokens")
            
            Result.success(tokens)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch Decagon token list")
            Result.failure(e)
        }
    }
    
    /**
     * Validates swap parameters before quote fetching
     * 
     * Performs client-side validation to catch errors early:
     * - Amount must be > 0
     * - Slippage must be between 0.01% and 50%
     * - Input and output mints must be different
     * 
     * @param inputMint Input token mint
     * @param outputMint Output token mint
     * @param amount Swap amount
     * @param slippageBps Slippage in basis points
     * 
     * @return Result.success() if valid, Result.failure() with error message
     * 
     * Example:
     * ```
     * val validation = jupiterService.validateDecagonSwapParams(
     *     inputMint = solMint,
     *     outputMint = usdcMint,
     *     amount = 1_000_000_000,
     *     slippageBps = 50
     * )
     * if (validation.isFailure) {
     *     showError(validation.exceptionOrNull()?.message)
     * }
     * ```
     */
    fun validateDecagonSwapParams(
        inputMint: String,
        outputMint: String,
        amount: Long,
        slippageBps: Int
    ): Result<Unit> {
        return try {
            require(amount > 0) {
                "Swap amount must be greater than 0"
            }
            
            require(inputMint != outputMint) {
                "Input and output tokens must be different"
            }
            
            require(slippageBps in 1..5000) {
                "Slippage must be between 0.01% and 50%"
            }
            
            require(inputMint.isNotBlank() && outputMint.isNotBlank()) {
                "Token mint addresses cannot be empty"
            }
            
            Timber.d("Decagon swap parameters validated successfully")
            Result.success(Unit)
        } catch (e: IllegalArgumentException) {
            Timber.w("Invalid Decagon swap parameters: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Calculates effective price from quote
     * 
     * Returns the exchange rate between input and output tokens.
     * 
     * @param quote Swap quote
     * @return Price in output tokens per input token
     * 
     * Example:
     * ```
     * val price = jupiterService.calculateDecagonEffectivePrice(quote)
     * // For 1 SOL → 100 USDC swap, returns 100.0
     * ```
     */
    fun calculateDecagonEffectivePrice(quote: DecagonSwapQuoteResponse): Double {
        val inputAmount = quote.inAmount.toDoubleOrNull() ?: return 0.0
        val outputAmount = quote.outAmount.toDoubleOrNull() ?: return 0.0
        
        if (inputAmount == 0.0) return 0.0
        
        return outputAmount / inputAmount
    }
    
    /**
     * Checks if price impact is acceptable
     * 
     * Price impact is the difference between expected price
     * and actual execution price due to liquidity depth.
     * 
     * Recommended thresholds:
     * - < 1%: Good
     * - 1-5%: Acceptable (show warning)
     * - > 5%: High risk (require confirmation)
     * 
     * @param priceImpactPct Price impact percentage as string
     * @param maxAcceptable Maximum acceptable price impact (default: 5%)
     * @return true if price impact is acceptable
     * 
     * Example:
     * ```
     * if (!jupiterService.isDecagonPriceImpactAcceptable(quote.priceImpactPct, 5.0)) {
     *     showWarning("High price impact: ${quote.priceImpactPct}%")
     * }
     * ```
     */
    fun isDecagonPriceImpactAcceptable(
        priceImpactPct: String,
        maxAcceptable: Double = 5.0
    ): Boolean {
        val impact = priceImpactPct.toDoubleOrNull() ?: return false
        return impact <= maxAcceptable
    }
    
    /**
     * Formats route plan for display
     * 
     * Converts route plan into human-readable format showing
     * which DEXs are being used.
     * 
     * @param routePlan Route plan from quote
     * @return Formatted route description
     * 
     * Example:
     * ```
     * val route = jupiterService.formatDecagonRoutePlan(quote.routePlan)
     * // "Raydium (50%) → Orca (50%)"
     * ```
     */
    fun formatDecagonRoutePlan(routePlan: List<DecagonRoutePlan>): String {
        return routePlan.joinToString(" → ") { route ->
            val label = route.swapInfo.label ?: "Unknown DEX"
            val percent = route.percent
            "$label ($percent%)"
        }
    }
}