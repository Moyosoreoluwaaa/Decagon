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

class MainActivity : FragmentActivity() {

    private val walletRepository: DecagonWalletRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            // Check if wallet exists
            val hasWallet = walletRepository.getAllWallets()
                .first()
                .isNotEmpty()

            val startDestination = if (hasWallet) "wallet" else "onboarding"

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