package com.octane.wallet.domain.usecases.discover

import com.wallet.core.util.LoadingState
import com.wallet.domain.repository.DiscoverRepository

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
