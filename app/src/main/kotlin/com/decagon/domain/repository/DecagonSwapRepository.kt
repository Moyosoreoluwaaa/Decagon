package com.decagon.domain.repository

import com.decagon.data.remote.model.DecagonSwapQuoteResponse
import com.decagon.data.remote.model.DecagonSwapTransactionResponse
import com.decagon.data.remote.model.DecagonTokenInfo
import com.decagon.domain.model.DecagonSwapHistory
import kotlinx.coroutines.flow.Flow

/**
 * Decagon Swap Repository Interface
 * 
 * Domain layer contract for swap operations.
 * Implemented in data layer (DecagonSwapRepositoryImpl).
 * 
 * This repository abstracts:
 * - Jupiter API interactions
 * - Local swap history persistence
 * - Token list management
 * 
 * Responsibilities:
 * - Fetch swap quotes
 * - Build swap transactions
 * - Store swap history
 * - Manage token metadata
 */
interface DecagonSwapRepository {
    
    /**
     * Fetches swap quote from Jupiter Aggregator
     * 
     * @param inputMint Input token mint address
     * @param outputMint Output token mint address
     * @param amount Amount in smallest units (lamports/tokens)
     * @param slippageBps Slippage tolerance in basis points
     * 
     * @return Result containing quote or error
     */
    suspend fun getSwapQuote(
        inputMint: String,
        outputMint: String,
        amount: Long,
        slippageBps: Int = 50
    ): Result<DecagonSwapQuoteResponse>
    
    /**
     * Builds serialized swap transaction
     * 
     * @param userPublicKey User's wallet address
     * @param quote Quote from getSwapQuote()
     * @param priorityFeeLamports Priority fee for faster processing
     * 
     * @return Result containing transaction data or error
     */
    suspend fun buildSwapTransaction(
        userPublicKey: String,
        quote: DecagonSwapQuoteResponse,
        priorityFeeLamports: Long = 0
    ): Result<DecagonSwapTransactionResponse>
    
    /**
     * Fetches list of supported tokens
     * 
     * @param verifiedOnly Only return verified tokens
     * 
     * @return Result containing token list or error
     */
    suspend fun getTokenList(verifiedOnly: Boolean = true): Result<List<DecagonTokenInfo>>
    
    /**
     * Saves completed swap to local history
     * 
     * @param swap Swap history record
     */
    suspend fun saveSwapHistory(swap: DecagonSwapHistory)
    
    /**
     * Observes swap history for wallet
     * 
     * @param walletAddress Wallet address
     * 
     * @return Flow emitting swap history updates
     */
    fun observeSwapHistory(walletAddress: String): Flow<List<DecagonSwapHistory>>
    
    /**
     * Gets single swap by ID
     * 
     * @param swapId Swap identifier
     * 
     * @return Flow emitting swap or null
     */
    fun getSwapById(swapId: String): Flow<DecagonSwapHistory?>
    
    /**
     * Updates swap status
     * 
     * @param swapId Swap identifier
     * @param status New status (PENDING, CONFIRMED, FAILED)
     * @param signature Transaction signature (if confirmed)
     */
    suspend fun updateSwapStatus(
        swapId: String,
        status: String,
        signature: String? = null
    )
}