package com.decagon.data.repository

import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.core.crypto.DecagonSecureEnclaveManager
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.domain.repository.DecagonSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DecagonSettingsRepositoryImpl(
    private val walletDao: DecagonWalletDao,
    private val enclaveManager: DecagonSecureEnclaveManager,
    private val mnemonicHelper: DecagonMnemonic,
    private val keyDerivation: DecagonKeyDerivation,
    private val biometricAuthenticator: DecagonBiometricAuthenticator
) : DecagonSettingsRepository {

    init {
        Timber.d("DecagonSettingsRepositoryImpl initialized")
    }

    override suspend fun revealRecoveryPhrase(
        walletId: String,
        activity: FragmentActivity
    ): Result<String> = withContext(Dispatchers.Main) {
        Timber.d("Revealing recovery phrase for wallet: $walletId")

        try {
            val entity = withContext(Dispatchers.IO) {
                walletDao.getById(walletId).first()
                    ?: throw IllegalArgumentException("Wallet not found")
            }

            // CRITICAL: Store mnemonic separately during wallet creation
            // For Version 0.2, we'll return a stub message
            // Full implementation requires storing encrypted mnemonic in DB
            
            val alias = DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)
            
            // Authenticate before decryption
            val authenticated = suspendCancellableCoroutine { continuation ->
                biometricAuthenticator.authenticateForDecryption(
                    activity = activity,
                    title = "Reveal Recovery Phrase",
                    subtitle = "Authenticate to view your recovery phrase",
                    onSuccess = { continuation.resume(true) },
                    onError = { error ->
                        continuation.resumeWithException(
                            SecurityException("Authentication failed: $error")
                        )
                    }
                )
            }

            if (!authenticated) {
                throw SecurityException("Authentication required")
            }

            // Decrypt seed
            val seed = withContext(Dispatchers.Default) {
                enclaveManager.decryptSeed(alias, entity.encryptedSeed)
            }

            // STUB: In production, mnemonic should be stored encrypted separately
            // Cannot derive mnemonic from seed (one-way function)
            Timber.w("Recovery phrase reveal: Mnemonic not stored separately (Version 0.2 limitation)")
            
            Result.success(
                "Recovery phrase was not stored separately during wallet creation. " +
                "This feature requires wallet recreation in Version 0.3+"
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to reveal recovery phrase")
            Result.failure(e)
        }
    }

    override suspend fun revealPrivateKey(
        walletId: String,
        activity: FragmentActivity
    ): Result<String> = withContext(Dispatchers.Main) {
        Timber.d("Revealing private key for wallet: $walletId")

        try {
            val entity = withContext(Dispatchers.IO) {
                walletDao.getById(walletId).first()
                    ?: throw IllegalArgumentException("Wallet not found")
            }

            val alias = DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)
            
            // Authenticate before decryption
            val authenticated = suspendCancellableCoroutine { continuation ->
                biometricAuthenticator.authenticateForDecryption(
                    activity = activity,
                    title = "Reveal Private Key",
                    subtitle = "Authenticate to view your private key",
                    onSuccess = { continuation.resume(true) },
                    onError = { error ->
                        continuation.resumeWithException(
                            SecurityException("Authentication failed: $error")
                        )
                    }
                )
            }

            if (!authenticated) {
                throw SecurityException("Authentication required")
            }

            // Decrypt seed
            val seed = withContext(Dispatchers.Default) {
                enclaveManager.decryptSeed(alias, entity.encryptedSeed)
            }

            // Derive private key
            val (privateKey, _) = withContext(Dispatchers.Default) {
                keyDerivation.deriveSolanaKeypair(seed, entity.accountIndex)
            }

            val privateKeyHex = privateKey.joinToString("") { "%02x".format(it) }
            
            Timber.i("Private key revealed successfully")
            Result.success(privateKeyHex)
        } catch (e: Exception) {
            Timber.e(e, "Failed to reveal private key")
            Result.failure(e)
        }
    }

    override suspend fun updateWalletName(
        walletId: String,
        newName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        Timber.d("Updating wallet name: $walletId -> $newName")

        try {
            val entity = walletDao.getById(walletId).first()
                ?: throw IllegalArgumentException("Wallet not found")

            val updated = entity.copy(name = newName)
            walletDao.update(updated)

            Timber.i("Wallet name updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update wallet name")
            Result.failure(e)
        }
    }

    override suspend fun removeWallet(
        walletId: String,
        activity: FragmentActivity
    ): Result<Unit> = withContext(Dispatchers.Main) {
        Timber.d("Removing wallet: $walletId")

        try {
            // Authenticate before removal
            val authenticated = suspendCancellableCoroutine { continuation ->
                biometricAuthenticator.authenticateForDecryption(
                    activity = activity,
                    title = "Remove Account",
                    subtitle = "Authenticate to remove this account",
                    onSuccess = { continuation.resume(true) },
                    onError = { error ->
                        continuation.resumeWithException(
                            SecurityException("Authentication failed: $error")
                        )
                    }
                )
            }

            if (!authenticated) {
                throw SecurityException("Authentication required")
            }

            val entity = withContext(Dispatchers.IO) {
                walletDao.getById(walletId).first()
                    ?: throw IllegalArgumentException("Wallet not found")
            }

            // Delete wallet and key
            withContext(Dispatchers.IO) {
                walletDao.delete(entity)
            }

            val alias = DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)
            enclaveManager.deleteKey(alias)

            Timber.i("Wallet removed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove wallet")
            Result.failure(e)
        }
    }
}