package com.octane.wallet.domain.usecases.discover

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Token
import com.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe all tokens with automatic refresh.
 */
class ObserveTokensUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Token>>> {
        return repository.observeTokens()
    }
}
