package com.decagon

// :app/MainActivity.kt
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.ui.navigation.UnifiedNavHost
import com.decagon.ui.navigation.UnifiedRoute
import com.decagon.ui.theme.DecagonTheme
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : FragmentActivity() {
    private val walletRepository: DecagonWalletRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            enableEdgeToEdge()
            DecagonTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val startDestination by produceState<UnifiedRoute>(
                        initialValue = UnifiedRoute.Onboarding
                    ) {
                        value = walletRepository.getAllWallets().first().let { wallets ->
                            if (wallets.isEmpty()) {
                                Timber.i("No wallets - onboarding")
                                UnifiedRoute.Onboarding
                            } else {
                                Timber.i("Wallet exists - portfolio")
                                UnifiedRoute.Wallet
                            }
                        }
                    }

                    UnifiedNavHost(startDestination = startDestination)
                }
            }
        }
    }
}
