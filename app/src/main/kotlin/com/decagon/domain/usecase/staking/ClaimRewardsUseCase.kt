package com.decagon.domain.usecase.staking

import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.StakingRepository

/**
 * Claims accumulated staking rewards.
 * On Solana, rewards are auto-compounded, but this can be used
 * to withdraw rewards to the main wallet.
 * 
 * @param positionId Staking position ID
 * @return Result with Transaction or error
 */
class ClaimRewardsUseCase(
    private val stakingRepository: StakingRepository,
    private val transactionRepository: DecagonTransactionRepository
) {
    suspend operator fun invoke(positionId: String): Result<DecagonTransaction?> {
        return try {
            val transaction = stakingRepository.claimRewards(positionId)
            transactionRepository.insertTransaction(transaction)
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}