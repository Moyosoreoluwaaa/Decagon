package com.octane.wallet.data.service

import com.octane.wallet.data.remote.api.SwapAggregatorApi
import com.octane.wallet.data.remote.dto.swap.SwapQuoteResponse
import com.octane.wallet.data.remote.dto.swap.SwapRequest
import com.octane.wallet.data.remote.dto.swap.SwapTransactionResponse

/**
 * Real Jupiter Swap integration.
 * Fetches quotes and builds swap transactions.
 */
class JupiterSwapService(
    private val jupiterApi: SwapAggregatorApi
) {

    /**
     * Get best swap route from Jupiter aggregator.
     * 
     * @param inputMint Input token mint address (e.g., SOL: "So11111111111111111111111111111111111111112")
     * @param outputMint Output token mint address
     * @param amount Amount in smallest unit (lamports for SOL, base units for tokens)
     * @param slippageBps Slippage tolerance in basis points (50 = 0.5%)
     */
    suspend fun getSwapQuote(
        inputMint: String,
        outputMint: String,
        amount: Long,
        slippageBps: Int = 50
    ): Result<SwapQuoteResponse> {
        return try {
            val quote = jupiterApi.getQuote(
                inputMint = inputMint,
                outputMint = outputMint,
                amount = amount,
                slippageBps = slippageBps
            )
            Result.success(quote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get serialized swap transaction ready for signing.
     * 
     * @param userPublicKey User's wallet public key
     * @param quoteResponse Quote from getSwapQuote()
     * @param priorityFee Optional priority fee in lamports
     */
    suspend fun getSwapTransaction(
        userPublicKey: String,
        quoteResponse: SwapQuoteResponse,
        priorityFee: Long? = null
    ): Result<SwapTransactionResponse> {
        return try {
            val request = SwapRequest(
                userPublicKey = userPublicKey,
                quoteResponse = quoteResponse,
                prioritizationFeeLamports = priorityFee
            )

            val transaction = jupiterApi.getSwapTransaction(request)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculate price impact percentage.
     */
    fun calculatePriceImpact(
        inAmount: Double,
        outAmount: Double,
        inPrice: Double,
        outPrice: Double
    ): Double {
        val expectedOut = (inAmount * inPrice) / outPrice
        val actualOut = outAmount
        
        if (expectedOut == 0.0) return 0.0
        
        return ((expectedOut - actualOut) / expectedOut) * 100.0
    }

    /**
     * Calculate minimum output amount with slippage.
     */
    fun calculateMinimumOutput(
        expectedOutput: Double,
        slippagePercent: Double
    ): Double {
        return expectedOutput * (1.0 - (slippagePercent / 100.0))
    }

    /**
     * Common Solana token mints for quick reference.
     */
    object CommonMints {
        const val SOL = "So11111111111111111111111111111111111111112"
        const val USDC = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
        const val USDT = "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"
        const val BONK = "DezXAZ8z7PnrnRJjz3wXBoRgixCa6xjnB7YaB1pPB263"
        const val JUP = "JUPyiwrYJFskUPiHa7hkeR8VUtAeFoSYbKedZNsDvCN"
        const val RAY = "4k3Dyjzvzp8eMZWUXbBCjEvwSkkk59S5iCNLY3QrkX6R"
        const val ORCA = "orcaEKTdK7LKz57vaAYr9QeNsVEPfiu6QeMU1kektZE"
    }
}
