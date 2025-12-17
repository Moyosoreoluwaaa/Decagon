package com.octane.wallet.domain.usecases.wallet

import com.octane.wallet.core.security.KeystoreManager
import com.octane.wallet.domain.repository.WalletRepository
import kotlinx.coroutines.flow.first

/**
 * Deletes a wallet permanently.
 *
 * Business Rules:
 * - Cannot delete if it's the only wallet
 * - If deleting active wallet, activates next available wallet
 * - Deletes encrypted private key from secure storage
 * - Cascades deletion to assets and transactions (handled by database)
 */

class DeleteWalletUseCase(
    private val walletRepository: WalletRepository,
    private val keystoreManager: KeystoreManager
) {
    suspend operator fun invoke(walletId: String): Result<Unit> {
        return try {
            // Check wallet count
            val count = walletRepository.observeWalletCount().first()
            if (count <= 1) {
                return Result.failure(
                    IllegalStateException("Cannot delete the only wallet")
                )
            }

            // Check if deleting active wallet
            val wallet = walletRepository.getWalletById(walletId)
            val needsNewActive = wallet?.isActive == true

            // Delete private key from secure storage
            keystoreManager.deletePrivateKey(walletId)

            // Delete wallet from database
            walletRepository.deleteWallet(walletId)

            // Activate another wallet if needed
            if (needsNewActive) {
                val remainingWallets = walletRepository.observeAllWallets().first()
                remainingWallets.firstOrNull()?.let { nextWallet ->
                    walletRepository.setActiveWallet(nextWallet.id)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
