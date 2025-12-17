package com.octane.wallet.core.blockchain

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import kotlinx.serialization.Serializable

/**
 * Jupiter Aggregator API Service
 * 
 * Official Docs: https://station.jup.ag/api-v6/get-quote
 * Base URL: https://quote-api.jup.ag/v6
 * 
 * FEATURES:
 * - Best route aggregation (Raydium, Orca, Serum, etc.)
 * - Price impact calculation
 * - Slippage protection
 * - MEV protection (optional)
 */

interface JupiterApiService {
    
    /**
     * Get quote for token swap.
     * 
     * @param inputMint Input token mint address (e.g., SOL mint)
     * @param outputMint Output token mint address (e.g., USDC mint)
     * @param amount Amount in smallest unit (lamports for SOL)
     * @param slippageBps Slippage tolerance in basis points (100 = 1%)
     * @param onlyDirectRoutes Only use direct routes (faster but may be less optimal)
     * @param asLegacyTransaction Use legacy transaction format (for older wallets)
     * 
     * @return Quote with routes, price impact, fees
     */
    @GET("quote")
    suspend fun getQuote(
        @Query("inputMint") inputMint: String,
        @Query("outputMint") outputMint: String,
        @Query("amount") amount: Long,
        @Query("slippageBps") slippageBps: Int = 100,
        @Query("onlyDirectRoutes") onlyDirectRoutes: Boolean = false,
        @Query("asLegacyTransaction") asLegacyTransaction: Boolean = false
    ): JupiterQuoteResponse
    
    /**
     * Get swap transaction from quote.
     * 
     * @param request Swap request with quote and user public key
     * @return Serialized transaction ready to sign
     */
    @POST("swap")
    suspend fun getSwapTransaction(
        @Body request: JupiterSwapRequest
    ): JupiterSwapResponse
    
    /**
     * Get list of supported tokens.
     * 
     * @return List of token metadata (symbol, mint, decimals, logo)
     */
    @GET("tokens")
    suspend fun getTokens(): List<JupiterToken>
    
    /**
     * Get token price in USDC.
     * 
     * @param ids Comma-separated token mint addresses
     * @return Map of mint address to price
     */
    @GET("price")
    suspend fun getPrice(
        @Query("ids") ids: String
    ): Map<String, JupiterPrice>
}

// ==================== REQUEST/RESPONSE MODELS ====================

@Serializable
data class JupiterQuoteResponse(
    val inputMint: String,
    val inAmount: String,
    val outputMint: String,
    val outAmount: String,
    val otherAmountThreshold: String,
    val swapMode: String,
    val slippageBps: Int,
    val priceImpactPct: Double,
    val routePlan: List<JupiterRoutePlan>
)

@Serializable
data class JupiterRoutePlan(
    val swapInfo: JupiterSwapInfo,
    val percent: Int
)

@Serializable
data class JupiterSwapInfo(
    val ammKey: String,
    val label: String,
    val inputMint: String,
    val outputMint: String,
    val inAmount: String,
    val outAmount: String,
    val feeAmount: String,
    val feeMint: String
)

@Serializable
data class JupiterSwapRequest(
    val quoteResponse: JupiterQuoteResponse,
    val userPublicKey: String,
    val wrapAndUnwrapSol: Boolean = true,
    val useSharedAccounts: Boolean = true,
    val feeAccount: String? = null,
    val computeUnitPriceMicroLamports: Long? = null,
    val asLegacyTransaction: Boolean = false,
    val useTokenLedger: Boolean = false,
    val destinationTokenAccount: String? = null
)

@Serializable
data class JupiterSwapResponse(
    val swapTransaction: String,
    val lastValidBlockHeight: Long,
    val prioritizationFeeLamports: Long
)

@Serializable
data class JupiterToken(
    val address: String,
    val chainId: Int,
    val decimals: Int,
    val name: String,
    val symbol: String,
    val logoURI: String?,
    val tags: List<String>
)

@Serializable
data class JupiterPrice(
    val id: String,
    val mintSymbol: String,
    val vsToken: String,
    val vsTokenSymbol: String,
    val price: Double
)

