package com.octane.wallet.domain.usecases.discover

import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.repository.DiscoverRepository

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