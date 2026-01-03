package com.decagon.data.local.dao

import androidx.room.*
import com.decagon.data.local.entity.ApprovalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApprovalDao {
    
    @Query("""
        SELECT * FROM approvals 
        WHERE wallet_id = :walletId 
        AND is_revoked = 0
        ORDER BY approved_at DESC
    """)
    fun observeActiveApprovals(walletId: String): Flow<List<ApprovalEntity>>
    
    @Query("SELECT * FROM approvals WHERE id = :approvalId")
    suspend fun getApprovalById(approvalId: String): ApprovalEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApproval(approval: ApprovalEntity)
    
    @Query("""
        UPDATE approvals 
        SET is_revoked = 1, revoked_at = :revokedAt 
        WHERE id = :approvalId
    """)
    suspend fun revokeApproval(approvalId: String, revokedAt: Long)
}