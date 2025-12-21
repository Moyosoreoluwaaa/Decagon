package com.wallet.domain.usecases.wallet

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import com.wallet.core.blockchain.SolanaKeyGenerator
import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.domain.models.Wallet
import com.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.UUID

class CreateWalletUseCase(
    private val walletRepository: WalletRepository,
    private val keystoreManager: KeystoreManager,
    private val solanaKeyGenerator: SolanaKeyGenerator
) {
    suspend operator fun invoke(
        name: String,
        iconEmoji: String? = null,
        colorHex: String? = null
    ): Result<WalletCreationResult> {
        Timber.d("🔵 [CreateWallet] ========================================")
        Timber.d("🔵 [CreateWallet] START - name='$name', emoji=$iconEmoji")

        return try {
            // ✅ STEP 1: Validate input
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Wallet name cannot be empty"))
            }

            // ✅ STEP 2: Generate BIP39 mnemonic
            // we use the library's native char generation to ensure compatibility
            val mnemonicCode = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_12)

            // 🔴 FIX: Use String(chars) instead of joinToString(" ") to prevent encoding mismatches
            val seedPhrase = String(mnemonicCode.chars)

            Timber.d("✅ [CreateWallet] Mnemonic generated via chars")

            // ✅ STEP 3: Validate seed phrase (Self-check)
            // We do this to ensure the seed we just generated is valid before proceeding
            try {
                mnemonicCode.toSeed()
            } catch (e: Exception) {
                return Result.failure(IllegalStateException("Generated invalid seed phrase: ${e.message}"))
            }

            // ✅ STEP 4: Generate keypair
            Timber.d("🔵 [CreateWallet] STEP 4 - Generating Solana keypair from phrase...")

            val keypair = try {
                // Pass the trimmed seed phrase to ensure no accidental whitespace issues
                solanaKeyGenerator.fromSeedPhrase(seedPhrase.trim())
            } catch (e: Exception) {
                Timber.e(e, "❌ [CreateWallet] Keypair generation failed")
                return Result.failure(IllegalStateException("Failed to generate keypair: ${e.message}"))
            }

            // ✅ STEP 5: Check if first wallet
            val existingCount = walletRepository.observeWalletCount().first()
            val isFirstWallet = existingCount == 0

            // ✅ STEP 6: Create wallet model
            val walletId = UUID.randomUUID().toString()

            val wallet = Wallet(
                id = walletId,
                name = name,
                publicKey = keypair.publicKey,
                iconEmoji = iconEmoji ?: "🔥",
                colorHex = colorHex ?: "#4ECDC4",
                chainId = "solana",
                isActive = isFirstWallet,
                isHardwareWallet = false,
                hardwareDeviceName = null,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )

            // ✅ STEP 7: Store private key
            keystoreManager.storePrivateKey(wallet.id, keypair.privateKey).getOrThrow()

            // ✅ STEP 8: Save to database
            walletRepository.createWallet(wallet)

            val result = WalletCreationResult(
                wallet = wallet,
                seedPhrase = seedPhrase
            )

            Timber.d("🎉 [CreateWallet] SUCCESS - Wallet created: ${wallet.id}")
            Result.success(result)

        } catch (e: Exception) {
            Timber.e(e, "❌ [CreateWallet] FAILED: ${e.message}")
            Result.failure(e)
        }
    }
}

data class WalletCreationResult(
    val wallet: Wallet,
    val seedPhrase: String
)