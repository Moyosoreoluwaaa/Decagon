package com.octane.wallet.domain.usecases.preference

import com.octane.wallet.data.local.datastore.UserPreferencesStore
import kotlinx.coroutines.flow.Flow

/**
 * Toggles privacy mode (hides balances).
 */

class TogglePrivacyModeUseCase(
    private val preferencesStore: UserPreferencesStore
) {
    suspend operator fun invoke(enabled: Boolean) {
        preferencesStore.setPrivacyMode(enabled)
    }
}