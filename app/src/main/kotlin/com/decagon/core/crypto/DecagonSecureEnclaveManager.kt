package com.decagon.core.crypto

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import timber.log.Timber
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android Keystore wrapper for secure seed storage.
 * * Features:
 * - Hardware-backed encryption (keys never leave secure element)
 * - Biometric-protected decryption
 * - AES-256-GCM authenticated encryption
 * * Security: Private keys stored encrypted, decrypted only on-demand with biometric auth.
 */
class DecagonSecureEnclaveManager(private val context: Context) {

    init {
        Timber.d("DecagonSecureEnclaveManager initialized.") // Log added
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    /**
     * Encrypts wallet seed using Keystore.
     * * @param alias Unique identifier for this wallet
     * @param seed Raw BIP39 seed (64 bytes)
     * @return Encrypted seed (IV + ciphertext)
     * @throws SecurityException if encryption fails
     */
    fun encryptSeed(alias: String, seed: ByteArray): ByteArray {
        Timber.d("Encrypting seed for alias: $alias") // Log added
        return try {
            val key = getOrCreateKey(alias)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)

            // IV and ciphertext
            val iv = cipher.iv
            val ciphertext = cipher.doFinal(seed)

            // Result is IV followed by ciphertext
            Timber.i("Seed encrypted successfully for alias: $alias") // Log added
            iv + ciphertext
        } catch (e: Exception) {
            Timber.e(e, "Security failure: Failed to encrypt seed for alias: $alias") // Log added
            throw SecurityException("Failed to encrypt seed", e)
        }
    }

    /**
     * Decrypts wallet seed using Keystore.
     * * @param alias Unique identifier for this wallet
     * @param encryptedSeed Encrypted seed (IV + ciphertext)
     * @return Decrypted seed (64 bytes)
     * @throws SecurityException if decryption fails
     */
    fun decryptSeed(alias: String, encryptedSeed: ByteArray): ByteArray {
        Timber.d("Decrypting seed for alias: $alias") // Log added
        return try {
            val key = keyStore.getKey(alias, null) as? SecretKey
                ?: throw SecurityException("Key not found for alias: $alias")

            // IV is the first 12 bytes for AES/GCM/NoPadding
            val iv = encryptedSeed.sliceArray(0 until GCM_IV_LENGTH)
            val ciphertext = encryptedSeed.sliceArray(GCM_IV_LENGTH until encryptedSeed.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv))

            Timber.i("Seed decrypted successfully for wallet: $alias") // Log added
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            Timber.e(e, "Security failure: Failed to decrypt seed for alias: $alias") // Log added
            throw SecurityException("Failed to decrypt seed", e)
        }
    }

    /**
     * Deletes a key from the Android Keystore.
     * * @param alias Key alias to delete
     */
    fun deleteKey(alias: String) {
        Timber.w("Deleting key for alias: $alias") // Log added
        try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias)
                Timber.i("Key deleted successfully: $alias") // Log added
            } else {
                Timber.w("Attempted to delete non-existent key: $alias") // Log added
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete key for alias: $alias") // Log added
        }
    }

    /**
     * Retrieves an existing key or generates a new biometric-protected one.
     * * @param alias Unique identifier for this wallet's key
     * @return SecretKey for AES-256-GCM
     * @throws SecurityException if key generation fails
     */
    private fun getOrCreateKey(alias: String): SecretKey {
        Timber.d("Getting or creating key for alias: $alias") // Log added
        if (keyStore.containsAlias(alias)) {
            Timber.v("Key found for alias: $alias") // Log added
            return keyStore.getKey(alias, null) as SecretKey
        }

        Timber.i("Generating new biometric-protected key for alias: $alias") // Log added
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keySpec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                // CRITICAL: Require biometric auth for decryption
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationParameters(
                    30, // Valid for 30 seconds after biometric auth
                    KeyProperties.AUTH_BIOMETRIC_STRONG
                )
                // Prevent key extraction
                .setRandomizedEncryptionRequired(true)
                .build()
        } else {
            TODO("VERSION.SDK_INT < R")
        }

        keyGenerator.init(keySpec)
        val key = keyGenerator.generateKey()
        Timber.i("Key generation complete for alias: $alias") // Log added
        return key
    }

    // DecagonSecureEnclaveManager.kt

    /**
     * Creates a biometric-protected cipher for encryption.
     * This must be called from a BiometricPrompt to authenticate the user.
     *
     * @param alias Wallet key alias
     * @return Cipher ready for encryption (requires biometric auth)
     */
    fun createEncryptCipher(alias: String): Cipher {
        Timber.d("Creating encrypt cipher for alias: $alias (requires biometric)")
        val key = getOrCreateKey(alias)
        return Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, key)
        }
    }

    /**
     * Encrypts seed using an authenticated cipher.
     *
     * @param cipher Cipher from BiometricPrompt.CryptoObject
     * @param seed Raw seed bytes
     * @return Encrypted seed (IV + ciphertext)
     */
    fun encryptSeedWithCipher(cipher: Cipher, seed: ByteArray): ByteArray {
        Timber.d("Encrypting seed with authenticated cipher")
        val iv = cipher.iv
        val ciphertext = cipher.doFinal(seed)
        Timber.i("Seed encrypted successfully")
        return iv + ciphertext
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12 // 96 bits
        private const val GCM_TAG_LENGTH_BITS = 128

        // Key alias prefix for wallet seeds
        const val WALLET_KEY_PREFIX = "decagon_wallet_"

        /**
         * Generates key alias for wallet.
         * * @param walletId Unique wallet identifier
         * @return Keystore alias
         */
        fun getWalletKeyAlias(walletId: String): String {
            return "$WALLET_KEY_PREFIX$walletId"
        }
    }
}