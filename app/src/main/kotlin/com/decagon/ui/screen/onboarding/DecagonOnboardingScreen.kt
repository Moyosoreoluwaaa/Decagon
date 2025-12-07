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
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun DecagonOnboardingScreen(
    onWalletCreated: () -> Unit,
    viewModel: DecagonOnboardingViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    // ✅ Set activity immediately
    DisposableEffect(activity) {
        activity?.let { viewModel.setActivity(it) }
        onDispose { viewModel.setActivity(null) }
    }

    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is DecagonLoadingState.Idle -> {
            CreateWalletForm(
                onCreateWallet = { name ->
                    // ✅ Post to next frame to avoid fragment transaction conflict
                    activity?.window?.decorView?.post {
                        viewModel.createWallet(name)
                    }
                }
            )
        }

        is DecagonLoadingState.Loading -> {
            LoadingContent()
        }

        is DecagonLoadingState.Success -> {
            BackupMnemonicScreen(
                state = state.data as DecagonOnboardingViewModel.OnboardingState.WalletCreated,
                onAcknowledge = {
                    viewModel.acknowledgeBackup()
                    onWalletCreated()
                }
            )
        }

        is DecagonLoadingState.Error -> {
            ErrorContent(
                message = state.message,
                onRetry = { viewModel.createWallet("My Wallet") }
            )
        }
    }
}

@Composable
private fun CreateWalletForm(onCreateWallet: (String) -> Unit) {
    var walletName by remember { mutableStateOf("My Wallet") }

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
            onValueChange = { walletName = it },
            label = { Text("Wallet Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onCreateWallet(walletName) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Wallet")
        }
    }
}

@Composable
private fun BackupMnemonicScreen(
    state: DecagonOnboardingViewModel.OnboardingState.WalletCreated,
    onAcknowledge: () -> Unit
) {
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

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        words.chunked(3).forEach { rowWords ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowWords.forEachIndexed { index, word ->
                    Card(
                        modifier = Modifier.weight(1f)
                    ) {
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