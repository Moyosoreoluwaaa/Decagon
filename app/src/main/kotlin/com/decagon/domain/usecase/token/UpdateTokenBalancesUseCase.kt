package com.decagon.domain.usecase

import com.decagon.domain.model.TokenBalance
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.repository.SwapRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Updates token balances after swap/on-ramp operations.
 *
 * Flow:
 * 1. Get wallet by ID
 * 2. Fetch fresh balances from Jupiter
 * 3. Cache balances in Room (if TokenBalanceDao exists)
 *
 * Usage:
 * ```
 * val result = updateTokenBalancesUseCase(walletId)
 * if (result.isSuccess) {
 *     // Balances refreshed
 * }
 * ```
 */
class UpdateTokenBalancesUseCase(
    private val swapRepository: SwapRepository,
    private val walletRepository: DecagonWalletRepository
) {
    suspend operator fun invoke(walletId: String): Result<List<TokenBalance>> {
        return try {
            Timber.d("🔄 Updating token balances for wallet: $walletId")

            // Get wallet
            val wallet = walletRepository.getWalletById(walletId)
                .first { it != null }
                ?: return Result.failure(Exception("Wallet not found"))

            Timber.d("Wallet address: ${wallet.address.take(8)}...")

            // Fetch fresh balances from Jupiter Ultra API
            val balancesResult = swapRepository.getTokenBalances(wallet.address)

            balancesResult.onSuccess { balances ->
                Timber.i("✅ Token balances updated: ${balances.size} tokens")
                balances.forEach { balance ->
                    Timber.v("  - ${balance.symbol}: ${balance.uiAmount}")
                }
            }.onFailure { error ->
                Timber.e(error, "❌ Failed to fetch token balances")
            }

            balancesResult
        } catch (e: Exception) {
            Timber.e(e, "❌ Exception updating token balances")
            Result.failure(e)
        }
    }
}