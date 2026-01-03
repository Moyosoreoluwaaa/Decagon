package com.decagon.domain.usecase.staking

import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.repository.DecagonTransactionRepository
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.domain.repository.StakingRepository
import kotlinx.coroutines.flow.first

/**
 * Stakes tokens with a validator.
 * Creates a staking position and records the transaction.
 * 
 * @param validatorAddress Validator's public key
 * @param amountSol Amount to stake in SOL
 * @return Result with Transaction or error
 */
class StakeTokensUseCase(
    private val stakingRepository: StakingRepository,
    private val transactionRepository: DecagonTransactionRepository,
    private val walletRepository: DecagonWalletRepository
) {
    suspend operator fun invoke(
        validatorAddress: String,
        validatorName: String,
        amountSol: Double
    ): Result<DecagonTransaction?> {
        return try {
            val wallet = walletRepository.observeActiveWallet().first()
                ?: return Result.failure(IllegalStateException("No active wallet"))
            
            // Execute staking transaction
            val transaction = stakingRepository.stakeTokens(
                walletId = wallet.id,
                validatorAddress = validatorAddress,
                validatorName = validatorName,
                amount = amountSol
            )
            
            // Record transaction
            transactionRepository.insertTransaction(transaction)
            
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}