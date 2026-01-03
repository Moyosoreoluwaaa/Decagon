package com.decagon.domain.usecase.preference

import com.decagon.data.local.datastore.UserPreferencesStore
import kotlinx.coroutines.flow.Flow

/**
 * Observes user currency preference (USD, EUR, etc.).
 */
class ObserveCurrencyPreferenceUseCase(
    private val preferencesStore: UserPreferencesStore
) {
    operator fun invoke(): Flow<String> {
        return preferencesStore.currencyPreference
    }
}
