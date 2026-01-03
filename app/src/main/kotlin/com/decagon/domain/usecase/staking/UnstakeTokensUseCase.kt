package com.decagon.domain.usecase.staking

import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.StakingRepository

/**
 * Unstakes tokens from a validator.
 * Note: Solana has a cooldown period before tokens are available.
 * 
 * @param positionId Staking position ID to unstake
 * @return Result with Transaction or error
 */
class UnstakeTokensUseCase(
    private val stakingRepository: StakingRepository,
    private val transactionRepository: DecagonTransactionRepository
) {
    suspend operator fun invoke(positionId: String): Result<DecagonTransaction?> {
        return try {
            val transaction = stakingRepository.unstakeTokens(positionId)
            transactionRepository.insertTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}