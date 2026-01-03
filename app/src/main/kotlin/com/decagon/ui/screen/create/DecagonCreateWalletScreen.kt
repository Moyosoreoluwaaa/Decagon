package com.decagon.ui.screen.create

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy // Added import
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager // Added import
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString // Added import
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.decagon.core.util.DecagonLoadingState
import com.decagon.ui.screen.onboarding.DecagonOnboardingViewModel
import com.decagon.ui.theme.AppTypography
import kotlinx.coroutines.delay // Added import
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonCreateWalletScreen(
    onBackClick: () -> Unit,
    onWalletCreated: (mnemonic: String, address: String) -> Unit,
    viewModel: DecagonOnboardingViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    DisposableEffect(activity) {
        Timber.d("CreateWalletScreen: Setting activity")
        activity?.let { viewModel.setActivity(it) }
        onDispose {
            viewModel.setActivity(null)
            Timber.d("CreateWalletScreen: Cleared activity")
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    var walletName by remember { mutableStateOf("My Wallet") }

    // State for copy Snackbar
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    // Logic to dismiss Snackbar after a delay
    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            delay(2000)
            showCopiedSnackbar = false
        }
    }

    Timber.d("CreateWalletScreen state: ${uiState.javaClass.simpleName}")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Wallet", style = AppTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { // Added Snackbar host
            if (showCopiedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Copied to clipboard")
                }
            }
        }
    ) { padding ->
        when (val state = uiState) {
            is DecagonLoadingState.Idle -> {
                Timber.v("CreateWalletScreen: Showing form")
                CreateForm(
                    walletName = walletName,
                    onWalletNameChange = { walletName = it },
                    onCreate = {
                        Timber.i("CreateWalletScreen: onCreate triggered with name=$walletName")
                        activity?.window?.decorView?.post {
                            Timber.i("CreateWalletScreen: Calling viewModel.createWallet($walletName)")
                            viewModel.createWallet(walletName)
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            is DecagonLoadingState.Loading -> {
                Timber.i("CreateWalletScreen: Loading")
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DecagonLoadingState.Success -> {
                val walletState = state.data as DecagonOnboardingViewModel.OnboardingState.WalletCreated
                Timber.i("CreateWalletScreen: Success, showing mnemonic backup")

                BackupMnemonicScreen(
                    state = walletState,
                    onAcknowledge = {
                        Timber.i("CreateWalletScreen: Mnemonic acknowledged")
                        onWalletCreated(walletState.mnemonic, walletState.address)
                    },
                    onCopyMnemonic = { mnemonic -> // Pass copy action down
                        clipboardManager.setText(AnnotatedString(mnemonic))
                        showCopiedSnackbar = true
                    },
                    modifier = Modifier.padding(padding)
                )
            }

            is DecagonLoadingState.Error -> {
                Timber.e("CreateWalletScreen: Error - ${state.message}")
                ErrorScreen(
                    message = state.message,
                    onRetry = {
                        Timber.w("CreateWalletScreen: Retrying")
                        activity?.window?.decorView?.post {
                            viewModel.createWallet(walletName)
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun CreateForm(
    walletName: String,
    onWalletNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Your Wallet",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = walletName,
            onValueChange = onWalletNameChange,
            label = { Text("Wallet Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                Timber.d("CreateForm: Button clicked")
                onCreate()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Wallet")
        }
    }
}

@Composable
private fun BackupMnemonicScreen(
    state: DecagonOnboardingViewModel.OnboardingState.WalletCreated,
    onAcknowledge: () -> Unit,
    onCopyMnemonic: (String) -> Unit, // Added new parameter
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Backup Your Recovery Phrase",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Write down these 12 words in order. Keep them safe and secret.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        MnemonicGrid(mnemonic = state.mnemonic)

        Spacer(modifier = Modifier.height(32.dp))

        // Added Copy to Clipboard Button
        OutlinedButton(
            onClick = { onCopyMnemonic(state.mnemonic) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentCopy, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Copy to Clipboard")
        }

        Spacer(modifier = Modifier.height(16.dp)) // Adjust spacing after new button

        Text(
            text = "Address: ${state.address.take(4)}...${state.address.takeLast(4)}",
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onAcknowledge,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I've Saved My Phrase")
        }
    }
}

@Composable
private fun MnemonicGrid(mnemonic: String) {
    val words = mnemonic.split(" ")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        words.chunked(3).forEach { rowWords ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowWords.forEach { word ->
                    Card(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${words.indexOf(word) + 1}",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = word,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}