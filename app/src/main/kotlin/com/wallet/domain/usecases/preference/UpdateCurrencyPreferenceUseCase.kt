package com.octane.wallet.domain.usecases.preference

import com.octane.wallet.data.local.datastore.UserPreferencesStore
import kotlinx.coroutines.flow.Flow


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
