package com.octane.wallet.domain.usecases.discover

import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow

/**
 * Observe all dApps with automatic refresh.
 */
class ObserveDAppsUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<DApp>>> {
        return repository.observeDApps()
    }
}
