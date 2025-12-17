package com.octane.wallet.domain.repository

import com.octane.wallet.domain.models.Wallet
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for wallet operations.
 * Domain layer defines the contract, Data layer implements it.
 */

interface WalletRepository {
    fun observeAllWallets(): Flow<List<Wallet>>
    fun observeActiveWallet(): Flow<Wallet?>
    suspend fun getWalletById(walletId: String): Wallet?
    suspend fun getWalletByPublicKey(publicKey: String): Wallet? // ADDED
    suspend fun createWallet(wallet: Wallet)
    suspend fun updateWallet(wallet: Wallet)
    suspend fun setActiveWallet(walletId: String)
    suspend fun deleteWallet(walletId: String)
    fun observeWalletCount(): Flow<Int>
}
