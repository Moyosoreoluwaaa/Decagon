package com.octane.wallet.domain.repository

import com.octane.wallet.domain.models.Approval
import com.octane.wallet.domain.models.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing token approvals (spend allowances).
 * Handles approval tracking and revocation.
 */
interface ApprovalRepository {
    
    /**
     * Observes active approvals for a wallet.
     */
    fun observeApprovals(walletId: String): Flow<List<Approval>>
    
    /**
     * Gets a single approval by ID.
     */
    suspend fun getApprovalById(approvalId: String): Approval?
    
    /**
     * Revokes a token approval.
     * @return Transaction record of the revocation
     */
    suspend fun revokeApproval(approval: Approval): Transaction
    
    /**
     * Refreshes approval data from the network.
     */
    suspend fun refreshApprovals(walletId: String, publicKey: String)
}