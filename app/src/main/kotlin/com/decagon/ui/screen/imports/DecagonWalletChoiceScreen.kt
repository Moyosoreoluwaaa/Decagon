package com.decagon.ui.screen.imports

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import timber.log.Timber

@Composable
fun DecagonWalletChoiceScreen(
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit
) {
    Timber.d("WalletChoiceScreen: Displayed")
    
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
                Timber.i("WalletChoiceScreen: Create clicked")
                onCreateWallet()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Create New Wallet")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = {
                Timber.i("WalletChoiceScreen: Import clicked")
                onImportWallet()
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Import Existing Wallet")
        }
    }
}