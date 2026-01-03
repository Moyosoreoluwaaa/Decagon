package com.decagon.core.security

import android.content.Context
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager as AndroidBiometricManager

/**
 * Manages biometric authentication (FaceID/TouchID).
 * Auto-approves transactions < $100 when biometric succeeds.
 */
class BiometricManager(private val context: Context) {

    private val biometricManager = AndroidBiometricManager.from(context)

    /**
     * Check if biometric authentication is available.
     */
    fun isBiometricAvailable(): BiometricAvailability {
        return when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            AndroidBiometricManager.BIOMETRIC_SUCCESS ->
                BiometricAvailability.Available

            AndroidBiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricAvailability.NoHardware

            AndroidBiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricAvailability.HardwareUnavailable

            AndroidBiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricAvailability.NotEnrolled

            else ->
                BiometricAvailability.Unknown
        }
    }

    /**
     * Authenticate user with biometric prompt.
     * @param activity Host activity
     * @param config Authentication configuration
     * @param onSuccess Called when authentication succeeds
     * @param onError Called when authentication fails
     */
    fun authenticate(
        activity: FragmentActivity,
        config: BiometricConfig,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (errorCode: Int, errorMessage: String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    onSuccess(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    // User's biometric didn't match - they can try again
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(config.title)
            .setSubtitle(config.subtitle)
            .setDescription(config.description)
            .setNegativeButtonText(config.negativeButtonText)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    companion object {
        private const val AUTHENTICATORS =
            AndroidBiometricManager.Authenticators.BIOMETRIC_STRONG or
                    AndroidBiometricManager.Authenticators.DEVICE_CREDENTIAL
    }
}

sealed interface BiometricAvailability {
    data object Available : BiometricAvailability
    data object NoHardware : BiometricAvailability
    data object HardwareUnavailable : BiometricAvailability
    data object NotEnrolled : BiometricAvailability
    data object Unknown : BiometricAvailability

    val isAvailable: Boolean get() = this is Available
}

data class BiometricConfig(
    val title: String,
    val subtitle: String? = null,
    val description: String? = null,
    val negativeButtonText: String = "Cancel"
)