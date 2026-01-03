package com.decagon.domain.usecase.discover

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.DApp
import com.decagon.domain.model.DAppCategory
import com.decagon.domain.repository.DiscoverRepository
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