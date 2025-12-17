// app/core/data/remote/api/SolanaRpcApi.kt

package com.octane.wallet.data.remote.api

import com.octane.wallet.data.remote.dto.solana.BalanceResult
import com.octane.wallet.data.remote.dto.solana.BlockhashResult
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.octane.wallet.data.remote.dto.solana.RpcResponse
import com.octane.wallet.data.remote.dto.solana.SignatureInfo
import com.octane.wallet.data.remote.dto.solana.SignatureStatusesResult
import com.octane.wallet.data.remote.dto.solana.SignaturesParams
import com.octane.wallet.data.remote.dto.solana.SimulateParams
import com.octane.wallet.data.remote.dto.solana.SimulationResult
import com.octane.wallet.data.remote.dto.solana.TokenAccountsParams
import com.octane.wallet.data.remote.dto.solana.TokenAccountsResult
import com.octane.wallet.data.remote.dto.solana.TransactionResult
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.POST

/**
 * Solana RPC API using Ktorfit.
 * Handles all blockchain interactions (balance, transactions, simulation).
 */
interface SolanaRpcApi {

    /**
     * Get SOL balance for an address.
     */
    @POST(".")
    suspend fun getBalance(@Body request: RpcRequest<List<String>>): RpcResponse<BalanceResult>

    /**
     * Get SPL token balances for an address.
     */
    @POST(".")
    suspend fun getTokenAccountsByOwner(@Body request: RpcRequest<TokenAccountsParams>): RpcResponse<TokenAccountsResult>

    /**
     * Get transaction details.
     */
    @POST(".")
    suspend fun getTransaction(@Body request: RpcRequest<List<Any>>): RpcResponse<TransactionResult>

    /**
     * Send transaction to blockchain.
     */
    @POST(".")
    suspend fun sendTransaction(@Body request: RpcRequest<List<String>>): RpcResponse<String>

    /**
     * Simulate transaction (v1.6).
     */
    @POST(".")
    suspend fun simulateTransaction(@Body request: RpcRequest<SimulateParams>): RpcResponse<SimulationResult>

    /**
     * Get recent blockhash (for transaction building).
     */
    @POST(".")
    suspend fun getRecentBlockhash(@Body request: RpcRequest<List<String>>): RpcResponse<BlockhashResult>

    /**
     * Get transaction confirmation status.
     */
    @POST(".")
    suspend fun getSignatureStatuses(@Body request: RpcRequest<List<List<String>>>): RpcResponse<SignatureStatusesResult>

    /**
     * Get transaction history for address.
     */
    @POST(".")
    suspend fun getSignaturesForAddress(@Body request: RpcRequest<SignaturesParams>): RpcResponse<List<SignatureInfo>>
}