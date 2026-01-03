package com.decagon.domain.usecase.preference

import com.decagon.data.local.datastore.UserPreferencesStore


/**
 * Updates user currency preference.
 */
class UpdateCurrencyPreferenceUseCase (
    private val preferencesStore: UserPreferencesStore
) {
    suspend operator fun invoke(currency: String) {
        preferencesStore.setCurrency(currency)
    }
}
