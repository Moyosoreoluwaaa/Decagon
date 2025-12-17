package com.octane.wallet.domain.usecases.discover

import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Token
import com.octane.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe trending tokens (top 20 by market cap).
 */
class ObserveTrendingTokensUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Token>>> {
        return repository.observeTrendingTokens()
    }
}