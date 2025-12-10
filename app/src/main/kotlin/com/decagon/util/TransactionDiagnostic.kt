package com.decagon.util

import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.repository.DecagonTransactionRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * Diagnostic utility to check and fix stuck transactions.
 * 
 * Use this to investigate why transactions are stuck in PENDING.
 */
class TransactionDiagnostic(
    private val transactionRepository: DecagonTransactionRepository,
    private val rpcClient: SolanaRpcClient
) {

    /**
     * Diagnoses all pending transactions and updates their status.
     * 
     * @param walletAddress Address to check transactions for
     * @return Number of transactions updated
     */
    suspend fun diagnoseAndFixPending(walletAddress: String): Int {
        Timber.i("🔍 Starting transaction diagnostic for $walletAddress")
        
        val transactions = transactionRepository
            .getTransactionHistory(walletAddress)
            .first()
        
        val pendingTxs = transactions.filter { 
            it.status == com.decagon.domain.model.TransactionStatus.PENDING 
        }
        
        Timber.d("Found ${pendingTxs.size} pending transactions")
        
        var updatedCount = 0
        
        pendingTxs.forEach { tx ->
            Timber.d("Checking transaction: ${tx.id}")
            Timber.d("  Signature: ${tx.signature}")
            Timber.d("  Amount: ${tx.amount} SOL")
            Timber.d("  To: ${tx.to}")
            Timber.d("  Timestamp: ${tx.timestamp}")
            
            val signature = tx.signature
            
            if (signature.isNullOrBlank()) {
                Timber.e("❌ Transaction ${tx.id} has no signature! This is invalid.")
                Timber.e("   Marking as FAILED")
                
                transactionRepository.updateTransactionStatus(
                    txId = tx.id,
                    signature = "",
                    status = "FAILED"
                )
                updatedCount++
                return@forEach
            }
            
            // Check if transaction exists on-chain
            try {
                Timber.d("Checking on-chain status...")
                val statusResult = rpcClient.getTransactionStatus(signature)
                
                if (statusResult.isSuccess) {
                    val status = statusResult.getOrNull()
                    Timber.i("  On-chain status: $status")
                    
                    when (status) {
                        "processed", "confirmed", "finalized" -> {
                            Timber.i("✅ Transaction is CONFIRMED on-chain")
                            transactionRepository.updateTransactionStatus(
                                txId = tx.id,
                                signature = signature,
                                status = "CONFIRMED"
                            )
                            updatedCount++
                        }
                        "failed" -> {
                            Timber.w("❌ Transaction FAILED on-chain")
                            transactionRepository.updateTransactionStatus(
                                txId = tx.id,
                                signature = signature,
                                status = "FAILED"
                            )
                            updatedCount++
                        }
                        else -> {
                            Timber.d("Transaction still pending on-chain")
                            
                            // Check how old it is
                            val ageMinutes = (System.currentTimeMillis() - tx.timestamp) / 60000
                            if (ageMinutes > 5) {
                                Timber.w("⚠️ Transaction is ${ageMinutes} minutes old and still pending")
                                Timber.w("   This is unusual - likely dropped by network")
                                Timber.w("   Marking as FAILED")
                                
                                transactionRepository.updateTransactionStatus(
                                    txId = tx.id,
                                    signature = signature,
                                    status = "FAILED"
                                )
                                updatedCount++
                            }
                        }
                    }
                } else {
                    val error = statusResult.exceptionOrNull()
                    Timber.e(error, "Failed to check transaction status")
                    
                    // If we can't find the transaction after 2 minutes, mark as failed
                    val ageMinutes = (System.currentTimeMillis() - tx.timestamp) / 60000
                    if (ageMinutes > 2) {
                        Timber.w("⚠️ Cannot find transaction after ${ageMinutes} minutes")
                        Timber.w("   Likely never made it to network - marking as FAILED")
                        
                        transactionRepository.updateTransactionStatus(
                            txId = tx.id,
                            signature = signature,
                            status = "FAILED"
                        )
                        updatedCount++
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking transaction ${tx.id}")
            }
        }
        
        Timber.i("🏁 Diagnostic complete: $updatedCount/$${pendingTxs.size} transactions updated")
        return updatedCount
    }
    
    /**
     * Checks if a signature exists on Solana.
     * 
     * @param signature Transaction signature
     * @return true if found on-chain, false otherwise
     */
    suspend fun verifySignatureExists(signature: String): Boolean {
        return try {
            val result = rpcClient.getTransaction(signature)
            val details = result.getOrNull()
            
            if (details != null) {
                Timber.i("✅ Signature found on-chain:")
                Timber.i("   Slot: ${details.slot}")
                Timber.i("   Fee: ${details.fee} lamports")
                Timber.i("   Status: ${details.status}")
                true
            } else {
                Timber.w("❌ Signature NOT found on-chain")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error verifying signature")
            false
        }
    }
}