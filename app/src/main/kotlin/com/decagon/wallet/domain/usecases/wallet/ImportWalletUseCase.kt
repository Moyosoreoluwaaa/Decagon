package com.octane.wallet.domain.usecases.wallet

import com.octane.wallet.core.blockchain.SolanaKeyGenerator
import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.domain.models.Wallet
import com.octane.wallet.domain.repository.WalletRepository
import java.util.UUID

/**
 * Imports an existing wallet from seed phrase or private key.
 *
 * Business Rules:
 * - Validates seed phrase format (12 or 24 words)
 * - Checks if wallet already exists (prevents duplicates)
 * - Imports as inactive wallet (user must manually activate)
 * - Supports BIP39 seed phrases
 *
 * Usage:
 * ```
 * val result = importWalletUseCase(
 *     seedPhrase = "word1 word2 ... word12",
 *     name = "Imported Wallet"
 * )
 * ```
 */
class ImportWalletUseCase(
    private val walletRepository: WalletRepository,
    private val keystoreManager: KeystoreManager,
    private val solanaKeyGenerator: SolanaKeyGenerator
) {
    suspend operator fun invoke(
        seedPhrase: String,
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ): Result<Wallet> {
        return try {
            // Validate seed phrase
            val words = seedPhrase.trim().split("\\s+".toRegex())
            if (words.size != 12 && words.size != 24) {
                return Result.failure(
                    IllegalArgumentException("Seed phrase must be 12 or 24 words")
                )
            }

            // Derive keypair from seed phrase
            val keypair = solanaKeyGenerator.fromSeedPhrase(seedPhrase)

            // Check if wallet already exists
            val existingWallet = walletRepository.getWalletByPublicKey(keypair.publicKey)
            if (existingWallet != null) {
                return Result.failure(
                    IllegalStateException("Wallet already imported")
                )
            }

            // Create wallet model (not active by default)
            val wallet = Wallet(
                id = UUID.randomUUID().toString(),
                name = name.ifBlank { "Imported Wallet" },
                publicKey = keypair.publicKey,
                iconEmoji = iconEmoji ?: "📥",
                colorHex = colorHex ?: "#4ECDC4",
                chainId = "solana",
                isActive = false,
                isHardwareWallet = false,
                hardwareDeviceName = null,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )

            // Store encrypted private key
            keystoreManager.storePrivateKey(wallet.id, keypair.privateKey)
                .getOrThrow()

            // Save wallet
            walletRepository.createWallet(wallet)

            Result.success(wallet)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

