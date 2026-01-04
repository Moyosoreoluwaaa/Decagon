package com.decagon.ui.screen.lock

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import com.decagon.core.security.BiometricLockManager
import com.decagon.core.security.DecagonBiometricAuthenticator
import timber.log.Timber

class BiometricLockViewModel(
    private val biometricAuth: DecagonBiometricAuthenticator,
    private val lockManager: BiometricLockManager
) : ViewModel() {

    fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Timber.Forest.d("Initiating biometric unlock")

        biometricAuth.authenticateForDecryption(
            activity = activity,
            title = "Unlock Decagon",
            subtitle = "Authenticate to access your wallet",
            onSuccess = {
                Timber.Forest.i("Biometric unlock successful")
                onSuccess()
            },
            onError = { error ->
                Timber.Forest.e("Biometric unlock failed: $error")
                onError(error)
            }
        )
    }
}