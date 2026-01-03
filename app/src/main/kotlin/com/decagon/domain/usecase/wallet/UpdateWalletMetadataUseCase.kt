package com.decagon.domain.usecase.wallet

import com.decagon.domain.repository.DecagonWalletRepository

/**
 * Updates wallet metadata (name, emoji, color).
 * Used in WalletsViewModel for editing wallet details.
 */
class UpdateWalletMetadataUseCase(
    private val walletRepository: DecagonWalletRepository
) {
    suspend operator fun invoke(
        walletId: String,
        name: String? = null,
        iconEmoji: String? = null,
        colorHex: String? = null
    ): Result<Unit> {
        return try {
            TODO()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}