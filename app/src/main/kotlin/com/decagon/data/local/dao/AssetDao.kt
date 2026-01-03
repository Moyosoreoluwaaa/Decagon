package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.AssetEntity
import com.decagon.data.local.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    
    @Query("""
        SELECT * FROM assets 
        WHERE wallet_id = :walletId 
        AND is_hidden = 0 
        ORDER BY value_usd DESC
    """)
    fun observeAssets(walletId: String): Flow<List<AssetEntity>>
    
    @Query("""
        SELECT * FROM assets 
        WHERE wallet_id = :walletId 
        AND symbol = :symbol
    """)
    fun observeAsset(walletId: String, symbol: String): Flow<AssetEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: AssetEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssets(assets: List<AssetEntity>)
    
    @Query("UPDATE assets SET is_hidden = :isHidden WHERE id = :assetId")
    suspend fun updateVisibility(assetId: String, isHidden: Boolean)
    
    @Query("SELECT SUM(value_usd) FROM assets WHERE wallet_id = :walletId AND is_hidden = 0")
    fun observeTotalValueUsd(walletId: String): Flow<Double?>
}

// app/core/data/local/database/dao/ContactDao.kt

@Dao
interface ContactDao {
    
    @Query("SELECT * FROM contacts ORDER BY last_used DESC")
    fun observeContacts(): Flow<List<ContactEntity>>
    
    @Query("SELECT * FROM contacts WHERE address = :address")
    suspend fun getContactByAddress(address: String): ContactEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)
    
    @Query("UPDATE contacts SET last_used = :timestamp WHERE id = :contactId")
    suspend fun updateLastUsed(contactId: String, timestamp: Long)
    
    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: String)
}

//// app/core/data/local/database/dao/ApprovalDao.kt
//
//@Dao
//interface ApprovalDao {
//
//    @Query("""
//        SELECT * FROM approvals
//        WHERE wallet_id = :walletId
//        AND is_revoked = 0
//        ORDER BY approved_at DESC
//    """)
//    fun observeActiveApprovals(walletId: String): Flow<List<ApprovalEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertApproval(approval: ApprovalEntity)
//
//    @Query("UPDATE approvals SET is_revoked = 1, revoked_at = :timestamp WHERE id = :approvalId")
//    suspend fun revokeApproval(approvalId: String, timestamp: Long)
//}
//
//// app/core/data/local/database/dao/StakingDao.kt
//
//@Dao
//interface StakingDao {
//
//    @Query("""
//        SELECT * FROM staking_positions
//        WHERE wallet_id = :walletId
//        AND is_active = 1
//        ORDER BY staked_at DESC
//    """)
//    fun observeActivePositions(walletId: String): Flow<List<StakingPositionEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertPosition(position: StakingPositionEntity)
//
//    @Query("UPDATE staking_positions SET is_active = 0, unstaked_at = :timestamp WHERE id = :positionId")
//    suspend fun unstakePosition(positionId: String, timestamp: Long)
//
//    @Query("SELECT SUM(CAST(amount_staked AS REAL)) FROM staking_positions WHERE wallet_id = :walletId AND is_active = 1")
//    fun observeTotalStaked(walletId: String): Flow<Double?>
//}