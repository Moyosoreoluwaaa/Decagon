package com.decagon.domain.usecase.discover

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Token
import com.decagon.domain.repository.DiscoverRepository
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