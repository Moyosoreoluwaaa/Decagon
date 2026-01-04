package com.decagon.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.decagon.domain.model.DecagonWallet
import com.decagon.ui.navigation.UnifiedBottomNavBar
import com.decagon.ui.navigation.UnifiedRoute
import com.decagon.ui.screen.wallet.DecagonWalletViewModel
import com.decagon.ui.theme.AppTypography
import com.decagon.util.ItemShape
import org.koin.androidx.compose.koinViewModel

/**
 * Wallet-specific settings screen.
 * Shows settings for ONE wallet (mnemonic, private key, chains, remove).
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSettingsScreen(
    navController: NavController,
    viewModel: UnifiedSettingsViewModel = koinViewModel(),
    walletViewModel: DecagonWalletViewModel = koinViewModel()
) {
    val wallet by walletViewModel.walletState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var showRemoveDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    wallet?.let { activeWallet ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("${activeWallet.name} Settings",  style = AppTypography.titleLarge) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Profile header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ItemShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = activeWallet.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = activeWallet.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Active Wallet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Rounded.Edit, null)
                        }
                    }
                }

                // Blockchain Section
                SettingsSection(title = "Blockchain") {
                    SettingsItem(
                        icon = Icons.Rounded.Link,
                        title = "Manage Chains",
                        subtitle = "Switch between networks",
                        onClick = {
                            navController.navigate(UnifiedRoute.ManageChains(activeWallet.id))
                        }
                    )
                }

                // Security Section
                SettingsSection(title = "Security") {
                    SettingsItem(
                        icon = Icons.Rounded.Lock,
                        title = "Recovery Phrase",
                        subtitle = "View your 12-word seed phrase",
                        onClick = {
                            navController.navigate(UnifiedRoute.RevealRecovery(activeWallet.id))
                        }
                    )

                    SettingsItem(
                        icon = Icons.Rounded.Key,
                        title = "Private Key",
                        subtitle = "Export for external use",
                        onClick = {
                            navController.navigate(UnifiedRoute.RevealPrivateKey(activeWallet.id))
                        }
                    )
                }

                // Danger Zone
                SettingsSection(title = "Danger Zone") {
                    SettingsItem(
                        icon = Icons.Rounded.Delete,
                        title = "Remove Wallet",
                        subtitle = "Wipe this wallet from device",
                        onClick = { showRemoveDialog = true },
                        destructive = true
                    )
                }
            }
        }

        // Dialogs
        if (showEditDialog) {
            EditNameDialog(
                currentName = activeWallet.name,
                onDismiss = { showEditDialog = false },
                onConfirm = { newName ->
                    viewModel.updateWalletName(activeWallet.id, newName)
                    showEditDialog = false
                }
            )
        }

        if (showRemoveDialog) {
            AlertDialog(
                onDismissRequest = { showRemoveDialog = false },
                shape = ItemShape,
                icon = { Icon(Icons.Rounded.Warning, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Remove Wallet?") },
                text = { Text("This will remove the wallet from the app. Ensure you have your recovery phrase backed up.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRemoveDialog = false
                            activity?.let {
                                viewModel.removeWallet(activeWallet.id, it) {
                                    navController.navigate(UnifiedRoute.Onboarding) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Remove", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRemoveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = ItemShape,
        title = { Text("Edit Wallet Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Wallet Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { if (name.isNotBlank()) onConfirm(name) }
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp),
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = ItemShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    destructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (destructive) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (destructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontWeight = FontWeight.SemiBold
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}