package com.octane.wallet.domain.usecases.wallet

import com.wallet.core.blockchain.SolanaKeyGenerator
import com.octane.wallet.core.security.KeystoreManager
import com.wallet.domain.repository.WalletRepository

/**
 * Exports seed phrase for existing wallet (for backup).
 * Requires biometric authentication.
 */
class ExportSeedPhraseUseCase(
    private val walletRepository: WalletRepository,
    private val keystoreManager: KeystoreManager,
    private val solanaKeyGenerator: SolanaKeyGenerator
) {
    suspend operator fun invoke(walletId: String): Result<String> {
        return try {
            // Verify wallet exists
            val wallet = walletRepository.getWalletById(walletId)
                ?: return Result.failure(IllegalArgumentException("Wallet not found"))

            // Get encrypted private key
            val encryptedKey = keystoreManager.getPrivateKey(walletId).getOrThrow()
            
            // Decrypt private key
            val privateKey = keystoreManager.decryptPrivateKey(encryptedKey).getOrThrow()
            
            // Note: This is a limitation - we can't reverse private key to seed phrase
            // In a real implementation, you would:
            // 1. Store encrypted seed phrase separately during wallet creation
            // 2. Or use BIP39 derivation with stored seed
            
            // For now, return error - seed phrase export requires storing seed during creation
            Result.failure(
                UnsupportedOperationException(
                    "Seed phrase export not available for this wallet. " +
                    "Please create a new wallet and backup the seed phrase during creation."
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}