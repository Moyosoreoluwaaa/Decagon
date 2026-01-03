package com.decagon.domain.usecase

import com.decagon.core.network.RpcClientFactory
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Updates main wallet balance (SOL/ETH/MATIC) after on-ramp operations.
 *
 * Flow:
 * 1. Get wallet by ID
 * 2. Get active chain
 * 3. Create network-aware RPC client
 * 4. Fetch balance from blockchain
 *
 * Usage:
 * ```
 * val result = updateWalletBalanceUseCase(walletId, "solana")
 * if (result.isSuccess) {
 *     val balance = result.getOrNull()
 *     // Balance refreshed: 1.2345 SOL
 * }
 * ```
 */
class UpdateWalletBalanceUseCase(
    private val walletRepository: DecagonWalletRepository,
    private val rpcFactory: RpcClientFactory
) {
    suspend operator fun invoke(
        walletId: String,
        chainId: String
    ): Result<Double> {
        return try {
            Timber.d("🔄 Updating wallet balance: $walletId on $chainId")

            // Get wallet
            val wallet = walletRepository.getWalletById(walletId)
                .first { it != null }
                ?: return Result.failure(Exception("Wallet not found"))

            // Get chain-specific address
            val activeChain = wallet.chains.find { it.chainId == chainId }
                ?: return Result.failure(Exception("Chain $chainId not found in wallet"))

            Timber.d("Chain address: ${activeChain.address.take(8)}...")

            // Create network-aware RPC client
            val rpcClient = rpcFactory.createSolanaClient(chainId)

            // Fetch balance
            val balanceResult = rpcClient.getBalance(activeChain.address)

            balanceResult.mapCatching { lamports ->
                val solBalance = lamports / 1_000_000_000.0

                Timber.i("✅ Balance updated: $solBalance ${activeChain.chainType.symbol} on $chainId")
                solBalance
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update wallet balance")
            Result.failure(e)
        }
    }
}