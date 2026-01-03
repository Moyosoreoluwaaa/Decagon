package com.decagon.domain.usecase.discover

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Perp
import com.decagon.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Search perps by symbol.
 */
class SearchPerpsUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(query: String): Flow<LoadingState<List<Perp>>> {
        return repository.searchPerps(query)
    }
}