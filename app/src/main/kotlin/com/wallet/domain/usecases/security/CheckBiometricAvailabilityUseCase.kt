package com.octane.wallet.domain.usecases.security

import com.octane.wallet.core.security.BiometricAvailability
import com.octane.wallet.core.security.BiometricManager

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

