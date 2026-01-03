package com.decagon.domain.repository

import com.decagon.domain.model.StakingPosition
import com.decagon.domain.model.DecagonTransaction
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
     * @return DecagonTransaction record of the staking operation
     */
    suspend fun stakeTokens(
        walletId: String,
        validatorAddress: String,
        validatorName: String,
        amount: Double
    ): DecagonTransaction?
    
    /**
     * Unstakes tokens from a position.
     * Note: Solana has a cooldown period.
     * @return DecagonTransaction record of the unstaking operation
     */
    suspend fun unstakeTokens(positionId: String): DecagonTransaction?
    
    /**
     * Claims accumulated rewards from a position.
     * @return DecagonTransaction record of the claim operation
     */
    suspend fun claimRewards(positionId: String): DecagonTransaction?
    
    /**
     * Gets a single staking position by ID.
     */
    suspend fun getPositionById(positionId: String): StakingPosition?
    
    /**
     * Refreshes staking data from the network.
     */
    suspend fun refreshStakingData(walletId: String, publicKey: String)
}