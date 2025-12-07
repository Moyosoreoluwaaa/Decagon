package com.decagon.data.repository

import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.core.crypto.DecagonSecureEnclaveManager
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.data.mapper.DecagonWalletMapper.toDomain
import com.decagon.data.mapper.DecagonWalletMapper.toEntity
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.repository.DecagonWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class DecagonWalletRepositoryImpl(
    private val walletDao: DecagonWalletDao,
    private val enclaveManager: DecagonSecureEnclaveManager,
    private val mnemonicHelper: DecagonMnemonic,
    private val keyDerivation: DecagonKeyDerivation,
    private val biometricAuthenticator: DecagonBiometricAuthenticator
) : DecagonWalletRepository {

    init {
        Timber.d("DecagonWalletRepositoryImpl initialized.")
    }

    override suspend fun createWallet(
        name: String,
        mnemonic: String,
        accountIndex: Int,
        activity: FragmentActivity
    ): Result<DecagonWallet> = withContext(Dispatchers.Main) {  // ✅ Changed from IO
        Timber.d("Creating wallet: $name")

        try {
            require(mnemonicHelper.validatePhrase(mnemonic)) { "Invalid mnemonic" }

            // ✅ Offload crypto operations explicitly
            val seed = withContext(Dispatchers.Default) {
                mnemonicHelper.deriveSeed(mnemonic)
            }

            val (_, publicKey) = withContext(Dispatchers.Default) {
                keyDerivation.deriveSolanaKeypair(seed, accountIndex)
            }

            val address = keyDerivation.deriveSolanaAddress(publicKey)

            val walletId = UUID.randomUUID().toString()
            val alias = DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)

            Timber.i("Requesting biometric authentication...")

            // ✅ Now on Main thread
            val encryptedSeed = suspendCancellableCoroutine<ByteArray> { continuation ->
                biometricAuthenticator.authenticateForEncryption(
                    activity = activity,
                    alias = alias,
                    enclaveManager = enclaveManager,
                    seed = seed,
                    onSuccess = { encrypted ->
                        continuation.resume(encrypted)
                    },
                    onError = { error ->
                        continuation.resumeWithException(SecurityException("Auth failed: $error"))
                    }
                )
            }

            val wallet = DecagonWallet(
                id = walletId,
                name = name,
                publicKey = publicKey.toHex(),
                address = address,
                accountIndex = accountIndex,
                createdAt = System.currentTimeMillis()
            )

            // ✅ DB write on IO
            withContext(Dispatchers.IO) {
                val entity = wallet.toEntity(encryptedSeed)
                walletDao.insert(entity)
            }

            Timber.i("Wallet secured: $walletId")
            Result.success(wallet)
        } catch (e: Exception) {
            Timber.e(e, "Wallet creation failed")
            Result.failure(e)
        }
    }

    override fun getWalletById(id: String): Flow<DecagonWallet?> {
        Timber.d("Getting wallet by ID: $id")
        return walletDao.getById(id).map { it?.toDomain() }
    }

    override fun getAllWallets(): Flow<List<DecagonWallet>> {
        Timber.d("Getting all wallets.")
        return walletDao.getAll().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getActiveWallet(): Flow<DecagonWallet?> {
        Timber.d("Getting active wallet.")
        return walletDao.getActive().map { it?.toDomain() }
    }

    override suspend fun setActiveWallet(walletId: String) {
        Timber.i("Setting active wallet to ID: $walletId")
        walletDao.setActive(walletId)
    }

    override suspend fun deleteWallet(walletId: String) {
        Timber.w("Attempting to delete wallet with ID: $walletId")
        val wallet = walletDao.getById(walletId).first()
        wallet?.let {
            walletDao.delete(it)
            val keyAlias = DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)
            enclaveManager.deleteKey(keyAlias)
            Timber.i("Wallet $walletId deleted.")
        } ?: Timber.w("Attempted to delete non-existent wallet: $walletId")
    }

    override suspend fun decryptSeed(walletId: String): Result<ByteArray> {
        Timber.d("Attempting to decrypt seed for wallet: $walletId")
        return try {
            val entity = walletDao.getById(walletId).first()
                ?: return Result.failure(IllegalArgumentException("Wallet not found"))

            val keyAlias = DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)
            val seed = enclaveManager.decryptSeed(keyAlias, entity.encryptedSeed)

            Timber.i("Seed successfully decrypted for wallet: $walletId")
            Result.success(seed)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decrypt seed for wallet: $walletId")
            Result.failure(e)
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}