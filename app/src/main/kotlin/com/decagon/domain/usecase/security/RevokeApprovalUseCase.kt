package com.decagon.domain.usecase.security

import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.repository.ApprovalRepository
import com.decagon.domain.repository.DecagonTransactionRepository


/**
 * Revokes a token approval (spend allowance).
 * Executes a revocation transaction and marks the approval as revoked.
 * 
 * @param approvalId Approval database ID
 * @return Result with revocation Transaction or error
 */
class RevokeApprovalUseCase(
    private val approvalRepository: ApprovalRepository,
    private val transactionRepository: DecagonTransactionRepository
) {
    suspend operator fun invoke(approvalId: String): Result<DecagonTransaction?> {
        return try {
            // Get approval details
            val approval = approvalRepository.getApprovalById(approvalId)
                ?: return Result.failure(IllegalArgumentException("Approval not found"))
            
            // Execute revocation transaction
            val transaction = approvalRepository.revokeApproval(approval)
            
            // Record transaction
            transactionRepository.insertTransaction(transaction)
            
            Result.success(transaction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}