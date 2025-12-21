package com.wallet.domain.usecases.wallet

import com.wallet.domain.repository.WalletRepository


/**
 * Priority: HIGH (needed for VMs to work)
 */

// Set Active Wallet
class SetActiveWalletUseCase(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(walletId: String): Result<Unit> {
        return try {
            walletRepository.setActiveWallet(walletId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}