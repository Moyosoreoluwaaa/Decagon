package com.decagon.domain.usecase.wallet

import com.decagon.domain.repository.DecagonWalletRepository

class UpdateWalletUseCase(
    private val walletRepository: DecagonWalletRepository
) {
    suspend operator fun invoke(
        walletId: String,
        name: String? = null,
        iconEmoji: String? = null,
        colorHex: String? = null
    ): Result<Unit> {
        return try {
            val wallet = walletRepository.getWalletById(walletId)

            TODO()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}