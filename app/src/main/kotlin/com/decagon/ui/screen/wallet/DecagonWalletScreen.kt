package com.decagon.ui.screen.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.decagon.core.util.DecagonLoadingState
import com.decagon.ui.components.DecagonWalletSelector
import com.decagon.ui.screen.send.DecagonSendSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonWalletScreen(
    viewModel: DecagonWalletViewModel = koinViewModel(),
    onCreateWallet: () -> Unit = {},
    onImportWallet: () -> Unit = {},
    onNavigateToSettings: (String) -> Unit = {}
) {
    val walletState by viewModel.walletState.collectAsState()
    val allWallets by viewModel.allWallets.collectAsState()
    var showSendSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = walletState) {
                        is DecagonLoadingState.Success -> {
                            DecagonWalletSelector(
                                currentWallet = state.data,
                                allWallets = allWallets,
                                onWalletSelected = { walletId ->
                                    viewModel.switchWallet(walletId)
                                },
                                onCreateWallet = onCreateWallet,
                                onImportWallet = onImportWallet
                            )
                        }
                        else -> Text("Decagon Wallet")
                    }
                },
                actions = {
                    // Avatar button
                    IconButton(
                        onClick = {
                            val state = walletState
                            if (state is DecagonLoadingState.Success) {
                                onNavigateToSettings(state.data.id)
                            }
                        }
                    ) {
                        when (val state = walletState) {
                            is DecagonLoadingState.Success -> {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = state.data.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            else -> {
                                Icon(Icons.Default.AccountCircle, "Settings")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (walletState is DecagonLoadingState.Success) {
                FloatingActionButton(
                    onClick = { showSendSheet = true }
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, "Send")
                }
            }
        }
    ) { padding ->
        when (val state = walletState) {
            is DecagonLoadingState.Loading -> {
                LoadingView(Modifier.padding(padding))
            }
            is DecagonLoadingState.Success -> {
                WalletContent(
                    wallet = state.data,
                    modifier = Modifier.padding(padding)
                )
            }
            is DecagonLoadingState.Error -> {
                ErrorView(
                    message = state.message,
                    modifier = Modifier.padding(padding)
                )
            }
            is DecagonLoadingState.Idle -> {}
        }
    }

    if (showSendSheet) {
        DecagonSendSheet(
            onDismiss = { showSendSheet = false }
        )
    }
}

@Composable
private fun WalletContent(
    wallet: com.decagon.domain.model.DecagonWallet,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = wallet.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "${wallet.balance} SOL",
                    style = MaterialTheme.typography.displayMedium
                )

                Text(
                    text = "~ $0.00",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Version 0.2: Multi-wallet + Settings",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Tap avatar to access settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorView(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}