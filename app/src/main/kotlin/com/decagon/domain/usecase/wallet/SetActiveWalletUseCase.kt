package com.decagon.domain.usecase.wallet

import com.decagon.domain.repository.DecagonWalletRepository


/**
 * Priority: HIGH (needed for VMs to work)
 */

// Set Active Wallet
class SetActiveWalletUseCase(
    private val walletRepository: DecagonWalletRepository
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