package com.decagon.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages secure storage of private keys using Android Keystore.
 * Private keys NEVER exist in memory unencrypted.
 */
class KeystoreManager(private val context: Context) {

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Store private key securely.
     * @param walletId Unique identifier for wallet
     * @param privateKey Raw private key bytes (will be encrypted)
     */
    fun storePrivateKey(walletId: String, privateKey: ByteArray): Result<Unit> {
        return try {
            // Generate encryption key in Keystore
            val secretKey = getOrCreateSecretKey(walletId)

            // Encrypt private key
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            val iv = cipher.iv
            val encryptedKey = cipher.doFinal(privateKey)

            // Store encrypted key + IV
            encryptedPrefs.edit()
                .putString("${walletId}_key", encryptedKey.toBase64())
                .putString("${walletId}_iv", iv.toBase64())
                .apply()

            // Clear plaintext from memory
            privateKey.fill(0)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to store key: ${e.message}", e))
        }
    }

    /**
     * Retrieve private key (remains encrypted until used).
     * @param walletId Wallet identifier
     * @return Encrypted private key data
     */
    fun getPrivateKey(walletId: String): Result<EncryptedPrivateKey> {
        return try {
            val encryptedKeyB64 = encryptedPrefs.getString("${walletId}_key", null)
                ?: return Result.failure(SecurityException("Private key not found"))

            val ivB64 = encryptedPrefs.getString("${walletId}_iv", null)
                ?: return Result.failure(SecurityException("IV not found"))

            Result.success(
                EncryptedPrivateKey(
                    walletId = walletId,
                    encryptedData = encryptedKeyB64.fromBase64(),
                    iv = ivB64.fromBase64()
                )
            )
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to retrieve key: ${e.message}", e))
        }
    }

    /**
     * Decrypt private key for immediate use (e.g., signing).
     * Key is cleared from memory after use.
     */
    fun decryptPrivateKey(encryptedKey: EncryptedPrivateKey): Result<ByteArray> {
        return try {
            val secretKey = getOrCreateSecretKey(encryptedKey.walletId)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, encryptedKey.iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedKey = cipher.doFinal(encryptedKey.encryptedData)
            Result.success(decryptedKey)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to decrypt key: ${e.message}", e))
        }
    }

    /**
     * Delete private key permanently.
     */
    fun deletePrivateKey(walletId: String): Result<Unit> {
        return try {
            // Remove from encrypted prefs
            encryptedPrefs.edit()
                .remove("${walletId}_key")
                .remove("${walletId}_iv")
                .apply()

            // Delete encryption key from Keystore
            keyStore.deleteEntry(walletId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(SecurityException("Failed to delete key: ${e.message}", e))
        }
    }

    /**
     * Check if private key exists for wallet.
     */
    fun hasPrivateKey(walletId: String): Boolean {
        return encryptedPrefs.contains("${walletId}_key")
    }

    private fun getOrCreateSecretKey(alias: String): SecretKey {
        if (keyStore.containsAlias(alias)) {
            return (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .setUserAuthenticationRequired(false) // Biometric handled separately
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val PREFS_NAME = "octane_secure_storage"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
    }
}

/**
 * Encrypted private key data.
 * Never store plaintext keys in this object.
 */
data class EncryptedPrivateKey(
    val walletId: String,
    val encryptedData: ByteArray,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as EncryptedPrivateKey
        return walletId == other.walletId
    }

    override fun hashCode(): Int = walletId.hashCode()
}

// Base64 helpers
private fun ByteArray.toBase64(): String =
    Base64.encodeToString(this, Base64.NO_WRAP)

private fun String.fromBase64(): ByteArray =
    Base64.decode(this, Base64.NO_WRAP)