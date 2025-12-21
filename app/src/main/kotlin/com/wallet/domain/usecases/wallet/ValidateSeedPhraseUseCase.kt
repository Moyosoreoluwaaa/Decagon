package com.octane.wallet.domain.usecases.wallet

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed

/**
 * Validates BIP39 seed phrase format and checksum.
 */
class ValidateSeedPhraseUseCase {
    operator fun invoke(seedPhrase: String): ValidationResult {
        val words = seedPhrase.trim().split("\\s+".toRegex())
        
        // Check word count
        if (words.size != 12 && words.size != 24) {
            return ValidationResult.Invalid("Seed phrase must be 12 or 24 words")
        }
        
        // Check for duplicates
        if (words.distinct().size != words.size) {
            return ValidationResult.Invalid("Seed phrase contains duplicate words")
        }
        
        // Validate BIP39 checksum
        return try {
            val mnemonicCode = Mnemonics.MnemonicCode(words.joinToString(" "))
            mnemonicCode.toSeed() // This will throw if invalid
            ValidationResult.Valid
        } catch (e: Exception) {
            ValidationResult.Invalid("Invalid seed phrase: ${e.message}")
        }
    }
}

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val reason: String) : ValidationResult
}