package com.decagon.data.repository

import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.DecagonKeyDerivation
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

            // âœ… Check if mnemonic exists
            if (entity.encryptedMnemonic == null) {
                Timber.w("Mnemonic not available for wallet created before v0.2.1")
                return@withContext Result.failure(
                    IllegalStateException(
                        "Recovery phrase not available.\n\n" +
                                "This wallet was created before Version 0.2.1 when recovery phrases weren't stored.\n\n" +
                                "You can:\n" +
                                "• Export the private key instead\n" +
                                "• Create a new wallet to enable recovery phrase backup"
                    )
                )
            }

            val mnemonicAlias = "${DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)}_mnemonic"

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

            // Decrypt mnemonic
            val mnemonicBytes = withContext(Dispatchers.Default) {
                enclaveManager.decryptSeed(mnemonicAlias, entity.encryptedMnemonic)
            }

            val mnemonic = String(mnemonicBytes, Charsets.UTF_8)

            Timber.i("Recovery phrase revealed successfully")
            Result.success(mnemonic)
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

            // Delete wallet and keys
            withContext(Dispatchers.IO) {
                walletDao.delete(entity)
            }

            val alias = DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)
            val mnemonicAlias = "${alias}_mnemonic"
            enclaveManager.deleteKey(alias)
            enclaveManager.deleteKey(mnemonicAlias)

            Timber.i("Wallet removed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to remove wallet")
            Result.failure(e)
        }
    }
}