package com.koin


import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.koin.navigation.NavGraph
import com.koin.ui.session.SessionViewModel
import com.koin.ui.theme.KoinTheme
import com.koin.util.NetworkMonitor
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

class KoinCryptoAppMainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
            Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoinApp()
        }
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KoinApp() {
    KoinTheme {

        val navController = rememberNavController()
        val sessionViewModel: SessionViewModel = koinViewModel()
        val isLoggedIn by sessionViewModel.isLoggedIn.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()

        // ✅ Add network monitor once here
        val networkMonitor = remember { NetworkMonitor(context) }
        val isNetworkAvailable by networkMonitor.isNetworkAvailable.collectAsState()

        // ✅ Track previous state to avoid repeated messages
        var previousNetworkState by rememberSaveable { mutableStateOf<Boolean?>(null) }

        // ✅ Toast logic here
        LaunchedEffect(isNetworkAvailable) {
            previousNetworkState?.let { previous ->
                if (!previous && isNetworkAvailable) {
                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar("Network restored ")
                        Toast.makeText(context, "Back online", Toast.LENGTH_SHORT).show()
                    }
                } else if (previous && !isNetworkAvailable) {
                    coroutineScope.launch {
//                        snackbarHostState.showSnackbar("No network  - Showing cached data")
                        Toast.makeText(
                            context,
                            "No internet connection, showing cached data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            previousNetworkState = isNetworkAvailable
        }

        fun showError(message: String?) {
            if (!message.isNullOrBlank()) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }


        val darkTheme = isSystemInDarkTheme()
        val bottomBarColor = MaterialTheme.colorScheme.background
        val view = LocalView.current

        SideEffect {
            val window = (view.context as ComponentActivity).window
            window.navigationBarColor = bottomBarColor.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars =
                !darkTheme
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !darkTheme
        }

        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            val paddingValues = innerPadding
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(),
                showError = ::showError
            )
        }
    }
}

