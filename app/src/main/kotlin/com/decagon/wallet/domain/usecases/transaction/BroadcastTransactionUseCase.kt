package com.octane.wallet.domain.usecases.transaction

import com.octane.wallet.core.blockchain.toBase64
import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.solana.RpcRequest

/**
 * Broadcasts a signed transaction to the Solana network.
 */
class BroadcastTransactionUseCase(
    private val solanaRpcApi: SolanaRpcApi
) {
    /**
     * Broadcast signed transaction to network.
     * @param signedTxBytes Signed transaction bytes
     * @return Transaction hash
     */
    suspend operator fun invoke(signedTxBytes: ByteArray): Result<String> {
        return try {
            val response = solanaRpcApi.sendTransaction(
                RpcRequest(
                    method = "sendTransaction",
                    params = listOf(signedTxBytes.toBase64())
                )
            )

            val txHash = response.result
                ?: return Result.failure(
                    Exception(response.error?.message ?: "Failed to broadcast transaction")
                )

            Result.success(txHash)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}