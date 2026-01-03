package com.decagon.domain.usecase.wallet

import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import timber.log.Timber

/**
 * Imports existing wallet from mnemonic phrase.
 * 
 * Validates phrase before import to prevent invalid wallet creation.
 */
class DecagonImportWalletUseCase(
    private val repository: DecagonWalletRepository,
    private val mnemonic: DecagonMnemonic
) {
    init {
        Timber.d("DecagonImportWalletUseCase initialized.")
    }

    /**
     * Imports wallet from mnemonic.
     * 
     * @param name Wallet name
     * @param phrase 12 or 24-word mnemonic phrase
     * @param accountIndex BIP44 account index (default: 0)
     * @param activity Activity for biometric auth
     * @return Result containing imported wallet
     */
    suspend operator fun invoke(
        name: String,
        phrase: String,
        accountIndex: Int = 0,
        activity: FragmentActivity
    ): Result<DecagonWallet> {
        Timber.d("Importing wallet: $name (account: $accountIndex)")
        
        return try {
            // Validate phrase first
            val cleanedPhrase = phrase.trim().lowercase()
            if (!mnemonic.validatePhrase(cleanedPhrase)) {
                Timber.w("Invalid mnemonic phrase provided")
                return Result.failure(IllegalArgumentException("Invalid mnemonic phrase"))
            }
            
            // Create wallet (same as create, but with user's phrase)
            val wallet = repository.createWallet(
                name = name,
                mnemonic = cleanedPhrase,
                accountIndex = accountIndex,
                activity = activity
            ).getOrThrow()
            
            Timber.i("Wallet imported successfully: ${wallet.id}")
            Result.success(wallet)
        } catch (e: Exception) {
            Timber.e(e, "Failed to import wallet")
            Result.failure(e)
        }
    }
    
    /**
     * Validates mnemonic phrase without creating wallet.
     * 
     * @param phrase Mnemonic phrase to validate
     * @return Result indicating if phrase is valid
     */
    fun validatePhrase(phrase: String): Result<Unit> {
        Timber.d("Validating mnemonic phrase")
        val cleanedPhrase = phrase.trim().lowercase()
        
        return if (mnemonic.validatePhrase(cleanedPhrase)) {
            Timber.i("Mnemonic phrase is valid")
            Result.success(Unit)
        } else {
            Timber.w("Mnemonic phrase is invalid")
            Result.failure(IllegalArgumentException("Invalid mnemonic phrase"))
        }
    }
    
    /**
     * Checks if phrase has correct word count.
     * 
     * @param phrase Mnemonic phrase
     * @return Expected word count (12 or 24), or null if invalid
     */
    fun getWordCount(phrase: String): Int? {
        val words = phrase.trim().split("\\s+".toRegex())
        Timber.v("Phrase contains ${words.size} words")
        return when (words.size) {
            12, 24 -> words.size
            else -> null
        }
    }
}