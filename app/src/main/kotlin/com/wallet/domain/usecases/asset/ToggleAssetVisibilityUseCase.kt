package com.octane.wallet.domain.usecases.asset

import com.wallet.domain.repository.AssetRepository

/**
 * Hides or shows an asset in portfolio view.
 *
 * Business Rules:
 * - Hidden assets still exist in database
 * - Hidden assets excluded from portfolio totals
 * - Can be unhidden later
 * - Useful for spam tokens or dust
 */

class ToggleAssetVisibilityUseCase(
    private val assetRepository: AssetRepository
) {
    suspend operator fun invoke(
        assetId: String,
        isHidden: Boolean
    ): Result<Unit> {
        return try {
            assetRepository.updateAssetVisibility(assetId, isHidden)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
