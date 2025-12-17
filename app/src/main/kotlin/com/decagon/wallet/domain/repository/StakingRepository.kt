package com.octane.wallet.domain.repository

import com.octane.wallet.domain.models.StakingPosition
import com.octane.wallet.domain.models.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing staking positions.
 * Handles staking, unstaking, and reward claiming.
 */
interface StakingRepository {
    
    /**
     * Observes all active staking positions for a wallet.
     */
    fun observeStakingPositions(walletId: String): Flow<List<StakingPosition>>
    
    /**
     * Observes total staked value for a wallet.
     */
    fun observeTotalStaked(walletId: String): Flow<Double>
    
    /**
     * Stakes tokens with a validator.
     * @return Transaction record of the staking operation
     */
    suspend fun stakeTokens(
        walletId: String,
        validatorAddress: String,
        validatorName: String,
        amount: Double
    ): Transaction
    
    /**
     * Unstakes tokens from a position.
     * Note: Solana has a cooldown period.
     * @return Transaction record of the unstaking operation
     */
    suspend fun unstakeTokens(positionId: String): Transaction
    
    /**
     * Claims accumulated rewards from a position.
     * @return Transaction record of the claim operation
     */
    suspend fun claimRewards(positionId: String): Transaction
    
    /**
     * Gets a single staking position by ID.
     */
    suspend fun getPositionById(positionId: String): StakingPosition?
    
    /**
     * Refreshes staking data from the network.
     */
    suspend fun refreshStakingData(walletId: String, publicKey: String)
}