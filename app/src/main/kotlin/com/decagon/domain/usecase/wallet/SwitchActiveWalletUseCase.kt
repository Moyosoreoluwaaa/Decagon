package com.decagon.domain.usecase.wallet

import com.decagon.data.local.datastore.UserPreferencesStore
import com.decagon.domain.repository.DecagonWalletRepository

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
    private val walletRepository: DecagonWalletRepository,
    private val userPreferencesStore: UserPreferencesStore
) {
    suspend operator fun invoke(walletId: String): Result<Unit> {
        return try {
            // Validate wallet exists
            val wallet = walletRepository.getWalletById(walletId)
            
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
