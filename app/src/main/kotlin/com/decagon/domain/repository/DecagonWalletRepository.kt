package com.decagon.domain.repository

import androidx.fragment.app.FragmentActivity
import com.decagon.domain.model.DecagonWallet
import kotlinx.coroutines.flow.Flow

/**
 * Wallet repository interface.
 *
 * Domain layer contract - implemented in data layer.
 */
interface DecagonWalletRepository {

    /**
     * Creates a new wallet.
     *
     * @param name User-friendly wallet name
     * @param mnemonic BIP39 mnemonic phrase
     * @param accountIndex BIP44 account index
     * @return Created wallet
     */
    suspend fun createWallet(
        name: String,
        mnemonic: String,
        accountIndex: Int = 0,
        activity: FragmentActivity
    ): Result<DecagonWallet>

    /**
     * Gets wallet by ID.
     *
     * @param id Wallet identifier
     * @return Flow emitting wallet or null
     */
    fun getWalletById(id: String): Flow<DecagonWallet?>

    /**
     * Gets all wallets.
     *
     * @return Flow emitting list of wallets
     */
    fun getAllWallets(): Flow<List<DecagonWallet>>

    /**
     * Gets active wallet.
     *
     * @return Flow emitting active wallet or null
     */
    fun getActiveWallet(): Flow<DecagonWallet?>

    // ✅ NEW: Returns cached balance immediately
    fun getActiveWalletCached(): Flow<DecagonWallet?>


    // ✅ NEW: Background refresh
    suspend fun refreshBalance(walletId: String)


    /**
     * Sets wallet as active.
     *
     * @param walletId Wallet ID to activate
     */
    suspend fun setActiveWallet(walletId: String)

    /**
     * Deletes a wallet.
     *
     * @param walletId Wallet ID to delete
     */
    suspend fun deleteWallet(walletId: String)

    /**
     * Decrypts wallet seed (requires biometric auth).
     *
     * @param walletId Wallet ID
     * @return Decrypted seed bytes
     */
    suspend fun decryptSeed(walletId: String): Result<ByteArray>

    suspend fun setActiveChain(walletId: String, chainId: String)

    fun observeAllWallets(): Flow<List<DecagonWallet>>
    fun observeActiveWallet(): Flow<DecagonWallet?>
}