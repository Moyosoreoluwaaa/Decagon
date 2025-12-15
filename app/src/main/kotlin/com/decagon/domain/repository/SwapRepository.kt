package com.decagon.domain.repository

import com.decagon.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SwapRepository {

    // Quote & Execute
    suspend fun getSwapQuote(
        inputMint: String,
        outputMint: String,
        amount: Long,
        userPublicKey: String,
        slippageBps: Int?
    ): Result<SwapOrder>

    suspend fun executeSwap(
        swapOrder: SwapOrder,
        signedTransaction: ByteArray
    ): Result<String>

    // Token Discovery
    suspend fun searchTokens(query: String, limit: Int = 20): Result<List<TokenInfo>>
    suspend fun getTokenBalances(publicKey: String): Result<List<TokenBalance>>
    suspend fun getTokenSecurity(mints: List<String>): Result<Map<String, List<SecurityWarning>>>

    // Swap History
    suspend fun saveSwapHistory(swap: SwapHistory)
    suspend fun updateSwapStatus(swapId: String, signature: String?, status: SwapStatus, error: String? = null)
    fun getSwapHistory(walletId: String): Flow<List<SwapHistory>>
    fun getSwapById(swapId: String): Flow<SwapHistory?>
}

