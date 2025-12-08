package com.decagon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.DecagonWallet

@Composable
fun DecagonWalletSelector(
    currentWallet: DecagonWallet,
    allWallets: List<DecagonWallet>,
    onWalletSelected: (String) -> Unit,
    onCreateWallet: () -> Unit,
    onImportWallet: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // Trigger button
        Surface(
            onClick = { expanded = true },
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = currentWallet.truncatedAddress,
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Switch wallet",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            // Existing wallets
            allWallets.forEach { wallet ->
                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = wallet.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = wallet.truncatedAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (wallet.id == currentWallet.id) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    onClick = {
                        if (wallet.id != currentWallet.id) {
                            onWalletSelected(wallet.id)
                        }
                        expanded = false
                    }
                )
            }

            Divider()

            // Create new wallet
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Create Wallet")
                    }
                },
                onClick = {
                    expanded = false
                    onCreateWallet()
                }
            )

            // Import wallet
            DropdownMenuItem(
                text = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("Import Wallet")
                    }
                },
                onClick = {
                    expanded = false
                    onImportWallet()
                }
            )
        }
    }
}