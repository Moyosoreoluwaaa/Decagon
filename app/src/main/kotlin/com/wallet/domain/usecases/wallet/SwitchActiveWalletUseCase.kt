package com.octane.wallet.domain.usecases.wallet

import com.octane.wallet.data.local.datastore.UserPreferencesStore
import com.wallet.domain.repository.WalletRepository

/**
 * Switches the active wallet.
 * 
 * Business Rules:
 * - Only one wallet can be active at a time
 * - Updates UI to reflect new active wallet
 * - Triggers asset refresh for new wallet
 * - Saves selection to preferences
 */
class SwitchActiveWalletUseCase (
    private val walletRepository: WalletRepository,
    private val userPreferencesStore: UserPreferencesStore
) {
    suspend operator fun invoke(walletId: String): Result<Unit> {
        return try {
            // Validate wallet exists
            val wallet = walletRepository.getWalletById(walletId)
                ?: return Result.failure(IllegalArgumentException("Wallet not found"))
            
            // Set as active
            walletRepository.setActiveWallet(walletId)
            
            // Save to preferences for persistence
            userPreferencesStore.setLastActiveWalletId(walletId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
