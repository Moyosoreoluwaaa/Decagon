package com.octane.wallet.domain.usecases.security

import androidx.fragment.app.FragmentActivity
import com.octane.wallet.core.security.BiometricConfig
import com.octane.wallet.core.security.BiometricManager
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Authenticates user with biometrics before sensitive operations.
 *
 * Use cases:
 * - Before showing seed phrase
 * - Before sending large amounts (> $100)
 * - Before deleting wallet
 * - App unlock after background
 */
class AuthenticateWithBiometricsUseCase(
    private val biometricManager: BiometricManager
) {
    /**
     * Authenticate user with biometrics.
     *
     * @param activity Current FragmentActivity (required for biometric prompt)
     * @param title Prompt title
     * @param subtitle Optional subtitle
     * @param description Optional description
     * @return Result<Unit> - Success if authenticated, Failure if cancelled/error
     */
    suspend operator fun invoke(
        activity: FragmentActivity,
        title: String = "Authenticate",
        subtitle: String? = null,
        description: String? = null
    ): Result<Unit> = suspendCancellableCoroutine { continuation ->
        biometricManager.authenticate(
            activity = activity,
            config = BiometricConfig(
                title = title,
                subtitle = subtitle,
                description = description
            ),
            onSuccess = {
                // onCancellation callback - cleanup if coroutine cancelled
                continuation.resume(Result.success(Unit)) { cause, _, _ -> // onCancellation callback - cleanup if coroutine cancelled
                    // onCancellation callback - cleanup if coroutine cancelled
                }
            },
            onError = { errorCode, message ->
                // onCancellation callback
                continuation.resume(
                    Result.failure(SecurityException("Biometric auth failed: $message"))
                ) { cause, _, _ -> // onCancellation callback
                    // onCancellation callback
                }
            }
        )
    }
}