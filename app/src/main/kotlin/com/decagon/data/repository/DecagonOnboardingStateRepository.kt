package com.decagon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first

interface DecagonOnboardingStateRepository {
    suspend fun hasCompletedOnboarding(): Boolean
    suspend fun markOnboardingComplete()
}

class DecagonOnboardingStateRepositoryImpl(
    private val dataStore: DataStore<Preferences>
) : DecagonOnboardingStateRepository {
    
    private val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
    
    override suspend fun hasCompletedOnboarding(): Boolean {
        return dataStore.data.first()[ONBOARDING_COMPLETE_KEY] ?: false
    }
    
    override suspend fun markOnboardingComplete() {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE_KEY] = true
        }
    }
}