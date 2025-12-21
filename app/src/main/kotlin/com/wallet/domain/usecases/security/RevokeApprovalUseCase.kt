package com.octane.wallet.domain.usecases.security

import com.octane.wallet.domain.models.Transaction
import com.wallet.domain.repository.ApprovalRepository
import com.wallet.domain.repository.TransactionRepository


/**
 * Revokes a token approval (spend allowance).
 * Executes a revocation transaction and marks the approval as revoked.
 * 
 * @param approvalId Approval database ID
 * @return Result with revocation Transaction or error
 */
class RevokeApprovalUseCase(
    private val approvalRepository: ApprovalRepository,
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(approvalId: String): Result<Transaction> {
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