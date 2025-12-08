package com.decagon

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.decagon.data.remote.SolanaRpcClient
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.ui.navigation.DecagonNavGraph
import com.decagon.ui.theme.DecagonTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class MainActivity : FragmentActivity() {

    private val walletRepository: DecagonWalletRepository by inject()
    private val rpcClient: SolanaRpcClient by inject() // ← ADD THIS


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            // Check if wallet exists
            val hasWallet = walletRepository.getAllWallets()
                .first()
                .isNotEmpty()

            val wallet = walletRepository.getActiveWallet().first()
            wallet?.let {
                val balanceResult = rpcClient.getBalance(it.address)
                Timber.d("Balance check: ${balanceResult.getOrNull()} lamports")
            }
            val startDestination = if (hasWallet) "wallet" else "choice"

            setContent {
                DecagonTheme {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        DecagonNavGraph(startDestination = startDestination)
                    }
                }
            }
        }
    }
}