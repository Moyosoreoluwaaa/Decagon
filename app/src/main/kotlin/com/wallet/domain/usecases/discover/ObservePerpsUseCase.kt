package com.octane.wallet.domain.usecases.discover

import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Perp
import com.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe all perps with automatic refresh.
 */
class ObservePerpsUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Perp>>> {
        return repository.observePerps()
    }
}