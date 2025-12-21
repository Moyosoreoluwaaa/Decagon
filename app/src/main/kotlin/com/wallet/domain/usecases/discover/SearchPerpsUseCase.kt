package com.octane.wallet.domain.usecases.discover

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Perp
import com.wallet.domain.repository.DiscoverRepository
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