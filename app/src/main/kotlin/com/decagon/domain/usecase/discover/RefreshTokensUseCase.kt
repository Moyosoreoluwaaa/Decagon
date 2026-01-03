package com.decagon.domain.usecase.discover

import com.decagon.core.util.LoadingState
import com.decagon.domain.repository.DiscoverRepository

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