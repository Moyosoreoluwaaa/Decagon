package com.wallet.domain.usecases.discover

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Token
import com.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * ✅ NEW: Observe ALL tokens (no 10-item limit).
 * Used by AllTokensScreen for full token list.
 */
class ObserveAllTokensUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Token>>> {
        return repository.observeAllTokens()
    }
}