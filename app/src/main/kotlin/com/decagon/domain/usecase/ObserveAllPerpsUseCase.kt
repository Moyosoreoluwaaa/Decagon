package com.decagon.domain.usecase

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Perp
import com.wallet.domain.repository.DiscoverRepository
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