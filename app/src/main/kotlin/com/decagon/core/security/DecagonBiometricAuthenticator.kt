package com.decagon.core.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.decagon.core.crypto.DecagonSecureEnclaveManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import javax.crypto.Cipher
import timber.log.Timber

/**
 * Biometric authentication wrapper.
 *
 * Handles:
 * - Fingerprint/Face authentication
 * - Hardware capability detection
 * - User enrollment status
 * - Crypto-backed authentication
 *
 * Security: Required before seed decryption.
 */
class DecagonBiometricAuthenticator(
    private val context: Context
) {
    private var currentActivity: FragmentActivity? = null

    fun setActivity(activity: FragmentActivity?) {
        currentActivity = activity
    }

    init {
        Timber.d("DecagonBiometricAuthenticator initialized.")
    }

    private val biometricManager = BiometricManager.from(context)

    /**
     * Checks if biometric authentication is available.
     *
     * @return BiometricStatus indicating availability
     */
    fun checkBiometricStatus(): BiometricStatus {
        Timber.d("Checking biometric status.")
        return when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                Timber.i("Biometric status: Available.")
                BiometricStatus.Available
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Timber.w("Biometric status: No Hardware.")
                BiometricStatus.NoHardware
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Timber.w("Biometric status: Hardware Unavailable.")
                BiometricStatus.HardwareUnavailable
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Timber.w("Biometric status: Not Enrolled.")
                BiometricStatus.NotEnrolled
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                Timber.w("Biometric status: Security Update Required.")
                BiometricStatus.SecurityUpdateRequired
            }
            else -> {
                Timber.e("Biometric status: Unknown.")
                BiometricStatus.Unknown
            }
        }
    }

    /**
     * Shows biometric prompt and authenticates user.
     *
     * Suspends until authentication succeeds or fails.
     *
     * @param activity Host activity for prompt
     * @param title Prompt title
     * @param subtitle Optional subtitle
     * @param description Optional description
     * @return true if authenticated successfully
     * @throws BiometricAuthException if authentication fails
     */
    suspend fun authenticate(
        activity: FragmentActivity,
        title: String = "Unlock Wallet",
        subtitle: String? = "Authenticate to access your wallet",
        description: String? = "Use your fingerprint or face to unlock"
    ): Boolean = suspendCancellableCoroutine { continuation ->
        Timber.d("Starting suspending biometric authentication (for decryption).")

        val executor = ContextCompat.getMainExecutor(context)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply {
                subtitle?.let { setSubtitle(it) }
                description?.let { setDescription(it) }
            }
            .setAllowedAuthenticators(AUTHENTICATORS)
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    Timber.i("Decryption authentication succeeded.")
                    if (continuation.isActive) {
                        continuation.resume(true)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Timber.e("Decryption authentication error: $errorCode - $errString")
                    if (continuation.isActive) {
                        val exception = when (errorCode) {
                            BiometricPrompt.ERROR_CANCELED,
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                                BiometricAuthException.UserCanceled(errString.toString())
                            }
                            BiometricPrompt.ERROR_LOCKOUT,
                            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                                BiometricAuthException.Lockout(errString.toString())
                            }
                            else -> {
                                BiometricAuthException.Failed(errString.toString())
                            }
                        }
                        continuation.resumeWithException(exception)
                    }
                }

                override fun onAuthenticationFailed() {
                    Timber.w("Decryption authentication failed.")
                    // Don't resume here - user can retry
                }
            }
        )

        // Cancel biometric prompt if coroutine is cancelled
        continuation.invokeOnCancellation {
            Timber.w("Decryption authentication cancelled by coroutine scope.")
            biometricPrompt.cancelAuthentication()
        }

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Authenticates user to encrypt wallet seed using a CryptoObject.
     *
     * @param activity Host activity for prompt (must be a FragmentActivity)
     * @param cipher Cipher from SecureEnclaveManager.createEncryptCipher()
     * @param onSuccess Called with authenticated cipher
     * @param onError Called on auth failure
     */
    fun authenticateForEncryption(
        activity: FragmentActivity,
        alias: String,
        enclaveManager: DecagonSecureEnclaveManager,
        seed: ByteArray,
        onSuccess: (ByteArray) -> Unit,
        onError: (String) -> Unit
    ) {
        Timber.d("Starting biometric auth for encryption")

        val executor = ContextCompat.getMainExecutor(context)

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Secure Your Wallet")
            .setSubtitle("Authenticate to encrypt wallet seed")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Timber.i("Auth succeeded, creating cipher NOW")
                    try {
                        // ✅ Create cipher AFTER authentication
                        val cipher = enclaveManager.createEncryptCipher(alias)
                        val encrypted = enclaveManager.encryptSeedWithCipher(cipher, seed)
                        onSuccess.invoke(encrypted)
                    } catch (e: Exception) {
                        Timber.e(e, "Encryption failed post-auth")
                        onError.invoke("Encryption failed: ${e.message}")
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Timber.e("Auth error: $errorCode - $errString")
                    onError.invoke(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    Timber.w("Auth failed (retry available)")
                }
            }
        )

        // ✅ No CryptoObject - authenticate first, create cipher in callback
        biometricPrompt.authenticate(promptInfo)
    }

    sealed class BiometricStatus {
        data object Available : BiometricStatus()
        data object NoHardware : BiometricStatus()
        data object HardwareUnavailable : BiometricStatus()
        data object NotEnrolled : BiometricStatus()
        data object SecurityUpdateRequired : BiometricStatus()
        data object Unknown : BiometricStatus()

        val isAvailable: Boolean get() = this is Available

        fun getUserMessage(): String = when (this) {
            is Available -> "Biometric authentication ready"
            is NoHardware -> "Device doesn't support biometric authentication"
            is HardwareUnavailable -> "Biometric hardware unavailable"
            is NotEnrolled -> "No biometric enrolled. Please add fingerprint or face in Settings"
            is SecurityUpdateRequired -> "Security update required"
            is Unknown -> "Biometric status unknown"
        }
    }

    sealed class BiometricAuthException(message: String) : Exception(message) {
        class UserCanceled(message: String) : BiometricAuthException(message)
        class Lockout(message: String) : BiometricAuthException(message)
        class Failed(message: String) : BiometricAuthException(message)
    }

    companion object {
        private const val AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_STRONG
    }
}