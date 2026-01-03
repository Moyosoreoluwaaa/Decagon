package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.StakingPositionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StakingDao {
    
    @Query("""
        SELECT * FROM staking_positions 
        WHERE wallet_id = :walletId 
        AND is_active = 1
        ORDER BY staked_at DESC
    """)
    fun observeActivePositions(walletId: String): Flow<List<StakingPositionEntity>>
    
    @Query("""
        SELECT SUM(CAST(amount_staked AS REAL)) 
        FROM staking_positions 
        WHERE wallet_id = :walletId 
        AND is_active = 1
    """)
    fun observeTotalStaked(walletId: String): Flow<Double?>
    
    @Query("SELECT * FROM staking_positions WHERE id = :positionId")
    suspend fun getPositionById(positionId: String): StakingPositionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosition(position: StakingPositionEntity)
    
    @Query("""
        UPDATE staking_positions 
        SET is_active = 0, unstaked_at = :unstakedAt 
        WHERE id = :positionId
    """)
    suspend fun unstakePosition(positionId: String, unstakedAt: Long)
}