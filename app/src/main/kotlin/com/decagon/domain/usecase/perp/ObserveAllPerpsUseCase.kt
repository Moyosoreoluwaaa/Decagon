package com.decagon.domain.usecase.perp

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Perp
import com.decagon.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * ✅ NEW: Observe ALL perps (no limit).
 * Used by AllPerpsScreen for full perp list.
 */
class ObserveAllPerpsUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Perp>>> {
        return repository.observeAllPerps()
    }
}