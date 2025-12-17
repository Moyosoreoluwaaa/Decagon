package com.octane.wallet.data.remote.api

import com.octane.wallet.data.remote.dto.swap.SwapQuoteResponse
import com.octane.wallet.data.remote.dto.swap.SwapRequest
import com.octane.wallet.data.remote.dto.swap.SwapTransactionResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query

/**
 * Jupiter Swap Aggregator API (v1.0).
 */
interface SwapAggregatorApi {
    
    /**
     * Get swap quote.
     */
    @GET("quote")
    suspend fun getQuote(
        @Query("inputMint") inputMint: String,
        @Query("outputMint") outputMint: String,
        @Query("amount") amount: Long,
        @Query("slippageBps") slippageBps: Int = 50
    ): SwapQuoteResponse
    
    /**
     * Get swap transaction (returns serialized transaction).
     */
    @POST("swap")
    suspend fun getSwapTransaction(@Body request: SwapRequest): SwapTransactionResponse
}
