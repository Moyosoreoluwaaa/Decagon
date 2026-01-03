package com.decagon.domain.usecase.security

import com.decagon.core.security.BiometricAvailability
import com.decagon.core.security.BiometricManager

/**
 * Checks if biometric authentication is available on device.
 */
class CheckBiometricAvailabilityUseCase(
    private val biometricManager: BiometricManager
) {
    operator fun invoke(): BiometricAvailability {
        return biometricManager.isBiometricAvailable()
    }
}

