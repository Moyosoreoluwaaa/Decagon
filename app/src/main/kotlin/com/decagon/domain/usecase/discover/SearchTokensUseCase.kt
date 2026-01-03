package com.decagon.domain.usecase.discover

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Token
import com.decagon.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Search tokens by name or symbol.
 */
class SearchTokensUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(query: String): Flow<LoadingState<List<Token>>> {
        return repository.searchTokens(query)
    }
}