package com.decagon.ui.screen.wallet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.decagon.ui.screen.send.DecagonSendSheet
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonWalletScreen(
    viewModel: DecagonWalletViewModel = koinViewModel()
) {
    val walletState by viewModel.walletState.collectAsState()
    var showSendSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Decagon Wallet") }
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
            text = "Version 0.2: Send tokens enabled!",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Tap FAB to send SOL",
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