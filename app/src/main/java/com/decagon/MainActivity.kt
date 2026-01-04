package com.decagon

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.decagon.core.security.BiometricLockManager
import com.decagon.core.security.DecagonBiometricAuthenticator
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.ui.navigation.UnifiedNavHost
import com.decagon.ui.navigation.UnifiedRoute
import com.decagon.ui.screen.lock.BiometricLockScreen
import com.decagon.ui.theme.DecagonTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : FragmentActivity() {
    private val walletRepository: DecagonWalletRepository by inject()
    private val biometricAuth: DecagonBiometricAuthenticator by inject()
    private val lockManager: BiometricLockManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set activity for biometric operations
        biometricAuth.setActivity(this)

        setContent {
            enableEdgeToEdge()
            DecagonTheme {
                val isLocked by lockManager.isLocked.collectAsState()

                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (isLocked) {
                        BiometricLockScreen(
                            onUnlock = {
                                lifecycleScope.launch {
                                    lockManager.unlockApp()
                                }
                            }
                        )
                    } else {
                        MainContent()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Check if should lock on resume
        lifecycleScope.launch {
            if (lockManager.shouldLockApp()) {
                Timber.i("App inactive too long, locking...")
                lockManager.lockApp()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        lockManager.onAppBackground()
    }

    override fun onDestroy() {
        super.onDestroy()
        biometricAuth.setActivity(null)
    }

    @Composable
    private fun MainContent() {
        // Synchronous wallet check (reads from Room cache)
        val hasWallet by produceState(initialValue = false) {
            value = walletRepository.getActiveWalletCached()
                .first() != null
        }

        val startDestination = if (hasWallet) {
            UnifiedRoute.Wallet
        } else {
            UnifiedRoute.Onboarding
        }

        UnifiedNavHost(startDestination = startDestination)
    }
}

