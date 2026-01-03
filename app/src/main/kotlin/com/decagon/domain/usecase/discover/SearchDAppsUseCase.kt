package com.decagon.domain.usecase.discover

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.DApp
import com.decagon.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Search dApps by name or description.
 */
class SearchDAppsUseCase(
    val repository:DiscoverRepository
) {
    operator fun invoke(query: String): Flow<LoadingState<List<DApp>>>

    {
        return repository.searchDApps(query)
    }
}
