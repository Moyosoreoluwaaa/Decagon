package com.decagon.data.repository

import androidx.fragment.app.FragmentActivity
import com.decagon.core.chains.ChainType
import com.decagon.core.crypto.DecagonKeyDerivation
import com.decagon.core.crypto.DecagonMnemonic
import com.decagon.core.crypto.DecagonSecureEnclaveManager
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.data.local.dao.DecagonWalletDao
import com.decagon.data.mapper.toDomain
import com.decagon.data.mapper.toEntity
import com.decagon.domain.model.ChainWallet
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
    ): Result<DecagonWallet> = withContext(Dispatchers.Main) {
        Timber.d("Creating multi-chain wallet: $name")

        try {
            require(mnemonicHelper.validatePhrase(mnemonic)) { "Invalid mnemonic" }

            val seed = withContext(Dispatchers.Default) {
                mnemonicHelper.deriveSeed(mnemonic)
            }

            // Derive all chains
            val allKeypairs = withContext(Dispatchers.Default) {
                keyDerivation.deriveAllChains(seed, accountIndex)
            }

            // Build ChainWallet list
            val chainWallets = allKeypairs.map { (chainId, keypair) ->
                val (privateKey, publicKey) = keypair
                val address = when (chainId) {
                    ChainType.Solana.id -> keyDerivation.deriveSolanaAddress(publicKey)
                    ChainType.Ethereum.id -> keyDerivation.deriveEthereumAddress(publicKey)
                    ChainType.Polygon.id -> keyDerivation.derivePolygonAddress(publicKey)
                    else -> throw IllegalStateException("Unknown chain: $chainId")
                }

                ChainWallet(
                    chainId = chainId,
                    address = address,
                    publicKey = publicKey.toHex(),
                    balance = 0.0
                )
            }

            val solanaChain = chainWallets.first { it.chainId == ChainType.Solana.id }
            val walletId = UUID.randomUUID().toString()
            val alias = DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)

            Timber.i("Requesting biometric authentication...")

            val (encryptedSeed, encryptedMnemonic) = suspendCancellableCoroutine { continuation ->
                biometricAuthenticator.authenticateForEncryption(
                    activity = activity,
                    alias = alias,
                    enclaveManager = enclaveManager,
                    seed = seed,
                    onSuccess = { encrypted ->
                        val mnemonicBytes = mnemonic.toByteArray(Charsets.UTF_8)
                        val mnemonicAlias = "${alias}_mnemonic"

                        try {
                            val cipher = enclaveManager.createEncryptCipher(mnemonicAlias)
                            val encryptedMnemonic = enclaveManager.encryptSeedWithCipher(cipher, mnemonicBytes)
                            continuation.resume(Pair(encrypted, encryptedMnemonic))
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        }
                    },
                    onError = { error ->
                        continuation.resumeWithException(SecurityException("Auth failed: $error"))
                    }
                )
            }

            val wallet = DecagonWallet(
                id = walletId,
                name = name,
                publicKey = solanaChain.publicKey,
                address = solanaChain.address,
                accountIndex = accountIndex,
                createdAt = System.currentTimeMillis(),
                chains = chainWallets,
                activeChainId = ChainType.Solana.id
            )

            withContext(Dispatchers.IO) {
                val entity = wallet.toEntity(encryptedSeed, encryptedMnemonic)
                walletDao.insert(entity)
                Timber.i("Wallet created with ${chainWallets.size} chains")
            }

            val walletCount = withContext(Dispatchers.IO) {
                walletDao.getCount().first()
            }

            if (walletCount == 1) {
                withContext(Dispatchers.IO) {
                    walletDao.setActive(walletId)
                }
            }

            Result.success(wallet)
        } catch (e: Exception) {
            Timber.e(e, "Wallet creation failed")
            Result.failure(e)
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

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
            val mnemonicAlias = "${keyAlias}_mnemonic"
            enclaveManager.deleteKey(keyAlias)
            enclaveManager.deleteKey(mnemonicAlias)
            Timber.i("Wallet $walletId deleted.")
        } ?: Timber.w("Attempted to delete non-existent wallet: $walletId")
    }

    override suspend fun setActiveChain(walletId: String, chainId: String) {
        withContext(Dispatchers.IO) {
            Timber.d("Setting active chain: $walletId -> $chainId")
            walletDao.updateActiveChain(walletId, chainId)
        }
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


    // âœ… NEW: Decrypt mnemonic
    suspend fun decryptMnemonic(walletId: String): Result<String> {
        Timber.d("Attempting to decrypt mnemonic for wallet: $walletId")
        return try {
            val entity = walletDao.getById(walletId).first()
                ?: return Result.failure(IllegalArgumentException("Wallet not found"))

            // Check if mnemonic exists (migration compatibility)
            if (entity.encryptedMnemonic == null) {
                return Result.failure(
                    IllegalStateException("Mnemonic not available (wallet created before Version 0.2.1)")
                )
            }

            val mnemonicAlias = "${DecagonSecureEnclaveManager.getWalletKeyAlias(walletId)}_mnemonic"
            val mnemonicBytes = enclaveManager.decryptSeed(mnemonicAlias, entity.encryptedMnemonic)
            val mnemonic = String(mnemonicBytes, Charsets.UTF_8)

            Timber.i("Mnemonic successfully decrypted for wallet: $walletId")
            Result.success(mnemonic)
        } catch (e: Exception) {
            Timber.e(e, "Failed to decrypt mnemonic for wallet: $walletId")
            Result.failure(e)
        }
    }

//    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}