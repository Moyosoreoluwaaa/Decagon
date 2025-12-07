package com.decagon.ui.screen.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.decagon.core.util.DecagonLoadingState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonWalletScreen(
    viewModel: DecagonWalletViewModel = koinViewModel()
) {
    val walletState by viewModel.walletState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Decagon Wallet") }
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
                    modifier = Modifier.padding(padding)
                )
            }
            
            is DecagonLoadingState.Error -> {
                ErrorView(
                    message = state.message,
                    modifier = Modifier.padding(padding)
                )
            }
            
            is DecagonLoadingState.Idle -> {
                // Should not happen
            }
        }
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
                    text = wallet.truncatedAddress,
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
            text = "Version 0.1: Wallet created successfully!",
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "Balance updates in version 0.2",
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