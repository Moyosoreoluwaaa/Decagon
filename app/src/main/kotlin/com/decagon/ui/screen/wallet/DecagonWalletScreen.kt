package com.decagon.ui.screen.wallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.decagon.core.util.DecagonLoadingState
import com.decagon.ui.components.DecagonQuickActions
import com.decagon.ui.components.DecagonWalletSelector
import com.decagon.ui.screen.send.DecagonSendSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonWalletScreen(
    viewModel: DecagonWalletViewModel = koinViewModel(),
    onCreateWallet: () -> Unit = {},
    onImportWallet: () -> Unit = {},
    onNavigateToSettings: (String) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToChains: () -> Unit = {}
) {
    val walletState by viewModel.walletState.collectAsState()
    val allWallets by viewModel.allWallets.collectAsState()

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
                    // ✅ NEW: History button
                    if (walletState is DecagonLoadingState.Success) {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.History, "Transaction History")
                        }
                    }

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
        }
    ) { padding ->
        when (val state = walletState) {
            is DecagonLoadingState.Loading -> {
                LoadingView(Modifier.padding(padding))
            }

            is DecagonLoadingState.Success -> {
                WalletContent(
                    wallet = state.data,
                    modifier = Modifier.padding(padding),
                    onNavigateToChains
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

}

@Composable
private fun WalletContent(
    wallet: com.decagon.domain.model.DecagonWallet,
    modifier: Modifier = Modifier,
    onNavigateToChains: () -> Unit = {}
) {

    var showSendSheet by remember { mutableStateOf(false) }

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
        Spacer(modifier = Modifier.height(16.dp))

        DecagonQuickActions(
            wallet = wallet,
            onSendClick = { showSendSheet = true },
            onReceiveClick = { /* show receive dialog */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateToChains,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage Chains")
            Icon(Icons.Default.ChevronRight, null)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Version 0.3: Transaction History",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Tap history icon to view transactions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    if (showSendSheet) {
        DecagonSendSheet(
            onDismiss = { showSendSheet = false }
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