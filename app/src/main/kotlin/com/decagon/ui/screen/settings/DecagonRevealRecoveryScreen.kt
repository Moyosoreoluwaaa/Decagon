package com.decagon.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.decagon.core.util.DecagonLoadingState
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonRevealRecoveryScreen(
    walletId: String,
    onBackClick: () -> Unit,
    viewModel: DecagonSettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    DisposableEffect(activity) {
        Timber.d("RevealRecoveryScreen: Setting activity")
        activity?.let { viewModel.setActivity(it) }
        onDispose {
            viewModel.setActivity(null)
            Timber.d("RevealRecoveryScreen: Cleared activity")
        }
    }

    val mnemonicState by viewModel.mnemonicState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    LaunchedEffect(walletId) {
        viewModel.loadMnemonic(walletId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetMnemonicState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Recovery Phrase") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = {
            if (showCopiedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Copied to clipboard")
                }
            }
        }
    ) { padding ->
        when (val state = mnemonicState) {
            is DecagonLoadingState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DecagonLoadingState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                ) {
                    // Warning card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Do not share your Recovery Phrase!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "If someone has your Recovery Phrase they will have full control of your wallet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Check if this is the stub message or actual mnemonic
                    if (state.data.contains("not stored separately")) {
                        // Show info card for Version 0.2 limitation
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Version 0.2 Limitation",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    state.data,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Your wallet is still secure. You can export the private key instead, or recreate your wallet in a future version to enable recovery phrase backup.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    } else {
                        // Show actual mnemonic grid
                        MnemonicGrid(mnemonic = state.data)

                        Spacer(modifier = Modifier.height(24.dp))

                        // Copy button
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(state.data))
                                showCopiedSnackbar = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy to Clipboard")
                        }
                    }
                }
            }

            is DecagonLoadingState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Button(onClick = onBackClick) {
                            Text("Go Back")
                        }
                    }
                }
            }

            DecagonLoadingState.Idle -> {}
        }
    }

    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            kotlinx.coroutines.delay(2000)
            showCopiedSnackbar = false
        }
    }
}

@Composable
private fun MnemonicGrid(mnemonic: String) {
    val words = mnemonic.split(" ")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        words.chunked(2).forEach { rowWords ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowWords.forEach { word ->
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${words.indexOf(word) + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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