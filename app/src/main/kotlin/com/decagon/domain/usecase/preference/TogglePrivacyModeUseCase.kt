package com.decagon.domain.usecase.preference

import com.decagon.data.local.datastore.UserPreferencesStore

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