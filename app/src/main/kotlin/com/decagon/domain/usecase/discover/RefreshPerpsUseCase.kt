package com.decagon.domain.usecase.discover

import com.decagon.core.util.LoadingState
import com.decagon.domain.repository.DiscoverRepository

/**
 * Manually refresh perps from API.
 */
class RefreshPerpsUseCase(
    private val repository: DiscoverRepository
) {
    suspend operator fun invoke(): LoadingState<Unit> {
        return repository.refreshPerps()
    }
}
