package com.octane.wallet.domain.usecases.transaction

import com.wallet.core.blockchain.SolanaKeyGenerator
import com.octane.wallet.core.security.KeystoreManager
import com.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first

/**
 * Signs a transaction with the active wallet's private key.
 * Used by DApp browser and transaction flows.
 */
class SignTransactionUseCase(
    private val keystoreManager: KeystoreManager,
    private val solanaKeyGenerator: SolanaKeyGenerator,
    private val walletRepository: WalletRepository
) {
    /**
     * Sign transaction bytes with active wallet.
     * @param txBytes Raw transaction bytes
     * @return Signature (64 bytes)
     */
    suspend operator fun invoke(txBytes: ByteArray): Result<ByteArray> {
        return try {
            // Get active wallet
            val wallet = walletRepository.observeActiveWallet().first()
                ?: return Result.failure(IllegalStateException("No active wallet"))

            // Retrieve encrypted private key
            val encryptedKey = keystoreManager.getPrivateKey(wallet.id).getOrThrow()
            
            // Decrypt private key
            val privateKey = keystoreManager.decryptPrivateKey(encryptedKey).getOrThrow()
            
            // Sign transaction
            val signature = solanaKeyGenerator.signTransaction(txBytes, privateKey)
            
            // CRITICAL: Clear private key from memory
            privateKey.fill(0)
            
            Result.success(signature)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign transaction with specific wallet ID.
     */
    suspend fun signWithWallet(walletId: String, txBytes: ByteArray): Result<ByteArray> {
        return try {
            val encryptedKey = keystoreManager.getPrivateKey(walletId).getOrThrow()
            val privateKey = keystoreManager.decryptPrivateKey(encryptedKey).getOrThrow()
            val signature = solanaKeyGenerator.signTransaction(txBytes, privateKey)
            privateKey.fill(0)
            Result.success(signature)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}