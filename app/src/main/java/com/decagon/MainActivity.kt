package com.decagon

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.decagon.domain.repository.DecagonWalletRepository
import com.decagon.ui.navigation.DecagonNavGraph
import com.decagon.ui.navigation.DecagonRoute
import com.decagon.ui.theme.DecagonTheme
import com.koin.util.NetworkMonitor
import com.octane.wallet.presentation.navigation.AppNavHost
import kotlinx.coroutines.flow.first
import org.koin.android.ext.android.inject
import timber.log.Timber

//class MainActivity : FragmentActivity() {
//
//    private val walletRepository: DecagonWalletRepository by inject()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        Timber.d("MainActivity onCreate")
//
//        setContent {
//            DecagonTheme {
//                Surface(
//                    color = MaterialTheme.colorScheme.background,
//                    modifier = Modifier.fillMaxSize()
//                ) {
//                    val context = LocalContext.current
//
//                    // Network monitoring
//                    val networkMonitor = remember { NetworkMonitor(context) }
//                    val isNetworkAvailable by networkMonitor.isNetworkAvailable.collectAsState()
//                    var previousNetworkState by rememberSaveable { mutableStateOf<Boolean?>(null) }
//
//                    // Network state change feedback
//                    LaunchedEffect(isNetworkAvailable) {
//                        previousNetworkState?.let { previous ->
//                            if (!previous && isNetworkAvailable) {
//                                Toast.makeText(context, "Back online", Toast.LENGTH_SHORT).show()
//                            } else if (previous && !isNetworkAvailable) {
//                                Toast.makeText(
//                                    context,
//                                    "No internet - showing cached data",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//                        }
//                        previousNetworkState = isNetworkAvailable
//                    }
//
//                    // Determine start destination based on wallet existence
//                    val startDestination by produceState<DecagonRoute>(
//                        initialValue = DecagonRoute.Onboarding
//                    ) {
//                        value = walletRepository.getAllWallets()
//                            .first()
//                            .let { wallets ->
//                                if (wallets.isEmpty()) {
//                                    Timber.i("No wallets found - showing onboarding")
//                                    DecagonRoute.Onboarding
//                                } else {
//                                    Timber.i("Wallet exists - showing portfolio")
//                                    DecagonRoute.Portfolio
//                                }
//                            }
//                    }
//
//                    DecagonNavGraph(startDestination = startDestination)
//                }
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        Timber.d("MainActivity onResume")
//    }
//
//    override fun onPause() {
//        super.onPause()
//        Timber.d("MainActivity onPause")
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        Timber.d("MainActivity onDestroy")
//    }
//}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DecagonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier
                    )
                }
            }
        }
    }
}