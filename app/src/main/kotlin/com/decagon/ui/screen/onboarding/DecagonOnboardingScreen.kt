package com.decagon.ui.screen.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.decagon.core.util.DecagonLoadingState
import com.decagon.ui.components.ImportWalletForm
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber // <-- Added Timber import

@Composable
fun DecagonOnboardingScreen(
    onWalletCreated: () -> Unit,
    viewModel: DecagonOnboardingViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var showingImport by remember { mutableStateOf(false) }
    Timber.d("DecagonOnboardingScreen Recomposition: showingImport=$showingImport")

    DisposableEffect(activity) {
        Timber.d("DisposableEffect triggered. Activity: ${activity?.javaClass?.simpleName ?: "null"}")
        activity?.let { viewModel.setActivity(it) }
        onDispose {
            viewModel.setActivity(null)
            Timber.d("DisposableEffect onDispose: Activity set to null.")
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val importState by viewModel.importState.collectAsState()
    Timber.v("Current uiState: $uiState | Current importState: $importState")

    when {
        // Show import UI if user chose import
        showingImport -> {
            Timber.d("Displaying Import Wallet flow. Current Import State: ${importState.javaClass.simpleName}")
            when (val state = importState) {
                is DecagonLoadingState.Idle -> {
                    Timber.v("Import State Idle: Showing ImportWalletForm.")
                    ImportWalletForm(
                        onImport = { name, mnemonic ->
                            Timber.i("User action: Attempting to import wallet. Name: $name")
                            // Post to decorView to ensure activity window is ready for biometric prompt
                            activity?.window?.decorView?.post {
                                viewModel.importWallet(name, mnemonic)
                            }
                        },
                        onBack = {
                            showingImport = false
                            Timber.i("User action: Back from Import Wallet flow.")
                        },
                        viewModel = viewModel
                    )
                }
                is DecagonLoadingState.Loading -> {
                    Timber.i("Import State Loading: Showing LoadingContent.")
                    LoadingContent()
                }
                is DecagonLoadingState.Success -> {
                    Timber.i("Import State Success: Wallet imported. Navigating away.")
                    // Navigate immediately on import success
                    LaunchedEffect(Unit) {
                        onWalletCreated()
                    }
                }
                is DecagonLoadingState.Error -> {
                    Timber.e("Import State Error: ${state.message}. Showing ErrorContent.")
                    ErrorContent(
                        message = state.message,
                        onRetry = { /* Reset handled by form */ }
                    )
                }
            }
        }

        // Show create UI flow
        else -> {
            Timber.d("Displaying Create Wallet flow. Current UI State: ${uiState.javaClass.simpleName}")
            when (val state = uiState) {
                is DecagonLoadingState.Idle -> {
                    Timber.v("UI State Idle: Showing WalletChoiceScreen.")
                    WalletChoiceScreen(
                        onCreateWallet = {
                            // showCreateForm handles the actual create trigger
                            showingImport = false
                            Timber.i("User action: Chose Create Wallet.")
                        },
                        onImportWallet = {
                            showingImport = true
                            Timber.i("User action: Chose Import Wallet.")
                        }
                    )
                }
                is DecagonLoadingState.Loading -> {
                    Timber.i("UI State Loading: Showing LoadingContent.")
                    LoadingContent()
                }
                is DecagonLoadingState.Success -> {
                    Timber.i("UI State Success: Wallet created. Showing BackupMnemonicScreen.")
                    BackupMnemonicScreen(
                        state = state.data as DecagonOnboardingViewModel.OnboardingState.WalletCreated,
                        onAcknowledge = {
                            Timber.i("User action: Acknowledged Mnemonic Backup. Navigating away.")
                            viewModel.acknowledgeBackup()
                            onWalletCreated()
                        }
                    )
                }
                is DecagonLoadingState.Error -> {
                    Timber.e("UI State Error: ${state.message}. Showing ErrorContent.")
                    ErrorContent(
                        message = state.message,
                        onRetry = {
                            Timber.w("User action: Retrying wallet creation.")
                            viewModel.createWallet("My Wallet")
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletChoiceScreen(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit
) {
    var walletName by remember { mutableStateOf("My Wallet") }
    var showCreateForm by remember { mutableStateOf(false) }

    if (showCreateForm) {
        Timber.v("WalletChoiceScreen: Showing CreateWalletForm.")
        CreateWalletForm(
            walletName = walletName,
            onWalletNameChange = {
                walletName = it
                Timber.v("CreateWalletForm: Wallet name changed to: $it")
            },
            onCreate = {
                onCreateWallet()
                Timber.i("CreateWalletForm: Create button clicked.")
                // ViewModel call is missing here, assuming it's done elsewhere or should be in onCreateWallet()
            },
            onBack = {
                showCreateForm = false
                Timber.i("CreateWalletForm: Back button clicked.")
            }
        )
    } else {
        Timber.v("WalletChoiceScreen: Showing initial choice buttons.")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Decagon Wallet",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Your Gateway to Web3",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    showCreateForm = true
                    Timber.i("WalletChoiceScreen: 'Create New Wallet' button clicked.")
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Create New Wallet")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onImportWallet,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Import Existing Wallet")
            }
        }
    }
}

@Composable
private fun CreateWalletForm(
    walletName: String,
    onWalletNameChange: (String) -> Unit,
    onCreate: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
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
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Wallet")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = onBack) {
            Text("Back")
        }
    }
}

@Composable
private fun BackupMnemonicScreen(
    state: DecagonOnboardingViewModel.OnboardingState.WalletCreated,
    onAcknowledge: () -> Unit
) {
    Timber.d("BackupMnemonicScreen displayed for wallet: ${state.walletId}")
    Column(
        modifier = Modifier
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
        words.chunked(3).forEachIndexed { index, rowWords ->
            Timber.v("MnemonicGrid: Rendering row ${index + 1} with ${rowWords.size} words.")
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
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Timber.d("LoadingContent displayed.")
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Timber.e("ErrorContent displayed with message: $message")
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