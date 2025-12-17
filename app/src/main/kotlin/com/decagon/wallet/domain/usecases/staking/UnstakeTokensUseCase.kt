package com.octane.wallet.domain.usecases.staking

import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.repository.StakingRepository
import com.octane.wallet.domain.repository.TransactionRepository

/**
 * Unstakes tokens from a validator.
 * Note: Solana has a cooldown period before tokens are available.
 * 
 * @param positionId Staking position ID to unstake
 * @return Result with Transaction or error
 */
class UnstakeTokensUseCase(
    private val stakingRepository: StakingRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(positionId: String): Result<Transaction> {
        return try {
            val transaction = stakingRepository.unstakeTokens(positionId)
            transactionRepository.insertTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}