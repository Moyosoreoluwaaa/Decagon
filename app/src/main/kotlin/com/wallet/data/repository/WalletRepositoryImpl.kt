package com.wallet.data.repository

import com.octane.wallet.data.local.database.dao.WalletDao
import com.octane.wallet.data.mappers.toDomain
import com.octane.wallet.data.mappers.toEntity
import com.octane.wallet.domain.models.Wallet
import com.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of WalletRepository.
 * Pure local storage (no remote sync needed for wallet metadata).
 */
class WalletRepositoryImpl(
    private val walletDao: WalletDao
) : WalletRepository {

    override fun observeAllWallets(): Flow<List<Wallet>> {
        return walletDao.observeAllWallets()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeActiveWallet(): Flow<Wallet?> {
        return walletDao.observeActiveWallet()
            .map { it?.toDomain() }
    }

    override suspend fun getWalletById(walletId: String): Wallet? {
        return walletDao.getWalletById(walletId)?.toDomain()
    }

    override suspend fun getWalletByPublicKey(publicKey: String): Wallet? {
        return walletDao.getWalletByPublicKey(publicKey)?.toDomain()
    }

    override suspend fun createWallet(wallet: Wallet) {
        walletDao.insertWallet(wallet.toEntity())
    }

    override suspend fun updateWallet(wallet: Wallet) {
        walletDao.updateWallet(wallet.toEntity())
    }

    override suspend fun setActiveWallet(walletId: String) {
        walletDao.setActiveWallet(walletId)
    }

    override suspend fun deleteWallet(walletId: String) {
        walletDao.deleteWallet(walletId)
    }

    override fun observeWalletCount(): Flow<Int> {
        return walletDao.observeWalletCount()
    }
}