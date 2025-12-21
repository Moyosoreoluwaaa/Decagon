package com.octane.wallet.domain.usecases.wallet

import com.wallet.domain.repository.WalletRepository

/**
 * Updates wallet metadata (name, emoji, color).
 * Used in WalletsViewModel for editing wallet details.
 */
class UpdateWalletMetadataUseCase(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(
        walletId: String,
        name: String? = null,
        iconEmoji: String? = null,
        colorHex: String? = null
    ): Result<Unit> {
        return try {
            val wallet = walletRepository.getWalletById(walletId)
                ?: return Result.failure(IllegalArgumentException("Wallet not found"))
            
            val updatedWallet = wallet.copy(
                name = name ?: wallet.name,
                iconEmoji = iconEmoji ?: wallet.iconEmoji,
                colorHex = colorHex ?: wallet.colorHex,
                lastUpdated = System.currentTimeMillis()
            )
            
            walletRepository.updateWallet(updatedWallet)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}