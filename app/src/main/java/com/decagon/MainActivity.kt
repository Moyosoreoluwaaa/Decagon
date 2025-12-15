package com.decagon

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.ui.navigation.DecagonNavGraph
import com.decagon.ui.theme.DecagonTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : FragmentActivity() {

    private val walletRepository: DecagonWalletRepository by inject()
    // ❌ REMOVED: Direct RpcClient injection
    // Balance checks are now handled by ViewModels with network-aware clients

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("MainActivity onCreate")

        lifecycleScope.launch {
            // Check if wallet exists
            val hasWallet = walletRepository.getAllWallets()
                .first()
                .isNotEmpty()

            Timber.d("Has wallet: $hasWallet")

            // ✅ REMOVED: Balance check from MainActivity
            // Let DecagonWalletViewModel handle balance fetching with network-aware RPC
            // This ensures correct network is used based on user settings

            val startDestination = if (hasWallet) "wallet" else "choice"
            Timber.i("Starting app at: $startDestination")

            setContent {
                DecagonTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        DecagonNavGraph(startDestination = startDestination)
                    }
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