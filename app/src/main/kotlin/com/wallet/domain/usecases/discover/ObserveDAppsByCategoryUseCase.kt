package com.octane.wallet.domain.usecases.discover

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.DAppCategory
import com.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe dApps filtered by category.
 */
class ObserveDAppsByCategoryUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(category: DAppCategory): Flow<LoadingState<List<DApp>>> {
        return repository.observeDAppsByCategory(category)
    }
}