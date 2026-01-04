package com.decagon.core.security

import com.decagon.data.local.datastore.UserPreferencesStore
import kotlinx.coroutines.flow.*
import timber.log.Timber

class BiometricLockManager(
    private val preferencesStore: UserPreferencesStore
) {
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var lastBackgroundTime = 0L

    /**
     * Check if app should lock on resume.
     */
    suspend fun shouldLockApp(): Boolean {
        val biometricEnabled = preferencesStore.biometricEnabled.first()
        val autoLockEnabled = preferencesStore.autoLockEnabled.first()

        if (!biometricEnabled || !autoLockEnabled) return false

        val timeoutSeconds = preferencesStore.autoLockTimeout.first().toIntOrNull() ?: 300
        val timeSinceBackground = System.currentTimeMillis() - lastBackgroundTime

        return timeSinceBackground > timeoutSeconds * 1000L
    }

    fun onAppBackground() {
        lastBackgroundTime = System.currentTimeMillis()
        Timber.d("App backgrounded at: $lastBackgroundTime")
    }

    suspend fun lockApp() {
        _isLocked.value = true
        Timber.i("App locked")
    }

    suspend fun unlockApp() {
        _isLocked.value = false
        Timber.i("App unlocked")
    }
}
