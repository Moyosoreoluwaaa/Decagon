package com.decagon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.decagon.core.network.FeatureFlags
import com.decagon.core.network.NetworkEnvironment
import com.decagon.core.network.NetworkManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun NetworkSwitcher(
    modifier: Modifier = Modifier,
    networkManager: NetworkManager = koinInject()
) {
    val currentNetwork by networkManager.currentNetwork.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = when (currentNetwork) {
                    NetworkEnvironment.MAINNET -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.tertiaryContainer
                }
            )
        ) {
            Text(currentNetwork.displayName)
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = "Switch network",
                modifier = Modifier.size(16.dp)
            )
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            NetworkEnvironment.values().forEach { network ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = network.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (network == currentNetwork) {
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                } else {
                                    androidx.compose.ui.text.font.FontWeight.Normal
                                }
                            )
                            if (network != NetworkEnvironment.MAINNET) {
                                Text(
                                    text = "View only - No transactions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        scope.launch {
                            networkManager.switchNetwork(network)
                        }
                        expanded = false
                    }
                )
            }
        }
    }
}