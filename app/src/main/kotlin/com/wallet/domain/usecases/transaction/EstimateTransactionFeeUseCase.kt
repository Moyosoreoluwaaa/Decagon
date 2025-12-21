package com.octane.wallet.domain.usecases.transaction

import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.wallet.domain.repository.WalletRepository

// Estimate Transaction Fee
class EstimateTransactionFeeUseCase(
    private val solanaRpcApi: SolanaRpcApi,
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(
        tokenSymbol: String,
        amount: Double
    ): Result<Double> {
        return try {
            // Get recent blockhash for fee calculation
            val response = solanaRpcApi.getRecentBlockhash(
                RpcRequest(method = "getRecentBlockhash", params = emptyList())
            )

            // Solana base fee is ~0.000005 SOL per signature
            // Add priority fee if network is congested
            val baseFee = 0.000005
            val priorityFee = calculatePriorityFee(response)

            Result.success(baseFee + priorityFee)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculatePriorityFee(response: Any): Double {
        // TODO: Implement priority fee calculation based on network load
        return 0.0
    }
}