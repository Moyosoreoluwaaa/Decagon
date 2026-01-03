package com.decagon.domain.usecase.discover

import com.decagon.core.util.LoadingState
import com.decagon.domain.repository.DiscoverRepository

/**
 * Manually refresh dApps from API.
 */
class RefreshDAppsUseCase(
    private val repository: DiscoverRepository
) {
    suspend operator fun invoke(): LoadingState<Unit> {
        return repository.refreshDApps()
    }
}