package com.decagon.core.crypto

import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import java.security.SecureRandom
import timber.log.Timber // Added Timber import

/**
 * BIP39 mnemonic generation and validation.
 *
 * Handles:
 * - Secure entropy generation
 * - Mnemonic phrase creation (12/24 words)
 * - Phrase validation
 * - Seed derivation for key generation
 *
 * Security: Uses SecureRandom for entropy, validates against BIP39 wordlist.
 */
class DecagonMnemonic {

    init {
        Timber.d("DecagonMnemonic initialized.") // Log initialization
    }

    /**
     * Generates a new 12-word mnemonic phrase.
     *
     * @return 12 space-separated words from BIP39 wordlist
     * @throws SecurityException if entropy generation fails
     */
    fun generate12WordPhrase(): String {
        Timber.d("Generating 12-word phrase (128 bits of entropy).")
        return try {
            val entropy = ByteArray(16) // 128 bits = 12 words
            SecureRandom().nextBytes(entropy)
            val mnemonicCode = Mnemonics.MnemonicCode(entropy)
            // Use chars property - contains the phrase as CharArray
            val phrase = mnemonicCode.chars.concatToString()
            Timber.v("Generated 12-word phrase: $phrase") // Log sensitive data verbosely
            phrase
        } catch (e: Exception) {
            Timber.e(e, "Security failure: Failed to generate 12-word mnemonic.")
            throw SecurityException("Failed to generate mnemonic", e)
        }
    }

    /**
     * Generates a new 24-word mnemonic phrase.
     *
     * @return 24 space-separated words from BIP39 wordlist
     * @throws SecurityException if entropy generation fails
     */
    fun generate24WordPhrase(): String {
        Timber.d("Generating 24-word phrase (256 bits of entropy).")
        return try {
            val entropy = ByteArray(32) // 256 bits = 24 words
            SecureRandom().nextBytes(entropy)
            val mnemonicCode = Mnemonics.MnemonicCode(entropy)
            // Use chars property - contains the phrase as CharArray
            val phrase = mnemonicCode.chars.concatToString()
            Timber.v("Generated 24-word phrase: $phrase") // Log sensitive data verbosely
            phrase
        } catch (e: Exception) {
            Timber.e(e, "Security failure: Failed to generate 24-word mnemonic.")
            throw SecurityException("Failed to generate mnemonic", e)
        }
    }

    /**
     * Validates a mnemonic phrase.
     *
     * Checks:
     * - Word count (12 or 24)
     * - All words in BIP39 wordlist
     * - Valid checksum
     *
     * @param phrase Space-separated mnemonic words
     * @return true if valid, false otherwise
     */
    fun validatePhrase(phrase: String): Boolean {
        Timber.d("Attempting to validate mnemonic phrase.")
        return try {
            val words = phrase.trim().split("\\s+".toRegex())

            // Check word count
            if (words.size != 12 && words.size != 24) {
                Timber.w("Validation failed: Incorrect word count (${words.size}).")
                return false
            }

            // Library validates on construction
            Mnemonics.MnemonicCode(phrase)
            Timber.i("Mnemonic phrase is valid.")
            true
        } catch (e: Exception) {
            Timber.w(e, "Mnemonic validation failed: Checksum or wordlist error.")
            false
        }
    }

    /**
     * Derives a seed from mnemonic phrase.
     *
     * This seed is used for BIP32/BIP44 key derivation.
     *
     * @param phrase Valid BIP39 mnemonic phrase
     * @param passphrase Optional BIP39 passphrase (default: empty)
     * @return 64-byte seed for key derivation
     * @throws IllegalArgumentException if phrase is invalid
     */
    fun deriveSeed(phrase: String, passphrase: String = ""): ByteArray {
        Timber.d("Deriving seed from mnemonic.")
        require(validatePhrase(phrase)) {
            Timber.e("Seed derivation failed due to invalid mnemonic phrase.")
            "Invalid mnemonic phrase"
        }

        return try {
            // Library expects phrase as String
            val mnemonicCode = Mnemonics.MnemonicCode(phrase)
            val seed = mnemonicCode.toSeed(passphrase.toCharArray())
            Timber.v("Seed successfully derived.") // Avoid logging seed as it's sensitive
            seed
        } catch (e: Exception) {
            Timber.e(e, "Failed to derive seed from mnemonic.")
            throw IllegalArgumentException("Failed to derive seed from mnemonic", e)
        }
    }

    /**
     * Splits phrase into individual words.
     * Useful for UI display (e.g., numbered word list).
     *
     * @param phrase Space-separated mnemonic
     * @return List of individual words
     */
    fun splitWords(phrase: String): List<String> {
        Timber.v("Splitting phrase into words.")
        return phrase.trim().split("\\s+".toRegex())
    }

    /**
     * Gets expected word count from entropy.
     *
     * @param entropyBits Entropy size in bits (128 or 256)
     * @return Expected word count (12 or 24)
     */
    fun getExpectedWordCount(entropyBits: Int): Int {
        Timber.d("Getting expected word count for ${entropyBits} bits of entropy.")
        return when (entropyBits) {
            128 -> 12
            256 -> 24
            else -> {
                Timber.e("Unsupported entropy size: $entropyBits")
                throw IllegalArgumentException("Unsupported entropy size: $entropyBits")
            }
        }
    }
}