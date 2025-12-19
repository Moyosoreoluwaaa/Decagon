package com.decagon

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.fragment.app.FragmentActivity
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.ui.navigation.DecagonNavGraph
import com.decagon.ui.navigation.DecagonRoute
import com.decagon.ui.theme.DecagonTheme
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : FragmentActivity() {

    private val walletRepository: DecagonWalletRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity onCreate")

        setContent {
            DecagonTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val startDestination by produceState<DecagonRoute>(
                        initialValue = DecagonRoute.Onboarding
                    ) {
                        value = walletRepository.getAllWallets()
                            .first()
                            .let { wallets ->
                                if (wallets.isEmpty()) {
                                    Timber.i("No wallets found - showing onboarding")
                                    DecagonRoute.Onboarding
                                } else {
                                    Timber.i("Wallet exists - showing portfolio")
                                    DecagonRoute.Portfolio
                                }
                            }
                    }

                    DecagonNavGraph(startDestination = startDestination)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Timber.d("MainActivity onResume")
    }

    override fun onPause() {
        super.onPause()
        Timber.d("MainActivity onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("MainActivity onDestroy")
    }
}