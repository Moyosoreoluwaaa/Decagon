package com.octane.wallet.domain.usecases.discover

import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Search dApps by name or description.
 */
class SearchDAppsUseCase(
    val repository:DiscoverRepository
) {
    operator fun invoke(query: String): Flow<LoadingState<kotlin.collections.List<DApp>>>

    {
        return repository.searchDApps(query)
    }
}
