package com.octane.wallet.domain.usecases.discover

import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.repository.DiscoverRepository

/**
 * Manually refresh tokens from API.
 */
class RefreshTokensUseCase(
    private val repository: DiscoverRepository
) {
    suspend operator fun invoke(): LoadingState<Unit> {
        return repository.refreshTokens()
    }
}