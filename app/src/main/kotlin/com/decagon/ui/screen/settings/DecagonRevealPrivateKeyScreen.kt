package com.decagon.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.decagon.core.util.DecagonLoadingState
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonRevealPrivateKeyScreen(
    walletId: String,
    onBackClick: () -> Unit,
    viewModel: DecagonSettingsViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    DisposableEffect(activity) {
        Timber.d("RevealPrivateKeyScreen: Setting activity")
        activity?.let { viewModel.setActivity(it) }
        onDispose {
            viewModel.setActivity(null)
            Timber.d("RevealPrivateKeyScreen: Cleared activity")
        }
    }

    val privateKeyState by viewModel.privateKeyState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    var isRevealed by remember { mutableStateOf(false) }

    LaunchedEffect(walletId) {
        viewModel.loadPrivateKey(walletId)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetPrivateKeyState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Private Key") },
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
        when (val state = privateKeyState) {
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
                                "Never share your Private Key!",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Anyone with your private key has FULL control of your wallet. Only use this for importing into other wallets.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Private key display
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Private Key (Hex)",
                                    style = MaterialTheme.typography.labelLarge
                                )

                                IconButton(onClick = { isRevealed = !isRevealed }) {
                                    Icon(
                                        if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (isRevealed) "Hide" else "Show"
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            SelectionContainer {
                                Text(
                                    text = if (isRevealed) {
                                        state.data
                                    } else {
                                        "•".repeat(64)
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Info card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "What is a Private Key?",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your private key is the cryptographic proof that you own this wallet. Unlike your recovery phrase, it's a single string that can be imported into compatible wallets.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

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