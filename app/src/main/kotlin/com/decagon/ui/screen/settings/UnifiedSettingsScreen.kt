package com.decagon.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.decagon.core.util.formatTimeout
import com.decagon.ui.components.CurrencyPickerDialog
import com.decagon.ui.components.TimeoutPickerDialog
import com.decagon.ui.navigation.UnifiedBottomNavBar
import com.decagon.ui.navigation.UnifiedRoute
import com.decagon.ui.theme.AppTypography
import com.decagon.util.ItemShape
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedSettingsScreen(
    navController: NavController,
    viewModel: UnifiedSettingsViewModel = koinViewModel()
) {
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val privacyMode by viewModel.privacyMode.collectAsState()
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val testnetEnabled by viewModel.testnetEnabled.collectAsState()
    val autoLockEnabled by viewModel.autoLockEnabled.collectAsState()
    val autoLockTimeout by viewModel.autoLockTimeout.collectAsState()
    val theme by viewModel.theme.collectAsState()

    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showTimeoutPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings",  style = AppTypography.titleLarge) }
            )
        },
        bottomBar = { UnifiedBottomNavBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Account Section
            SettingsSection(title = "Account") {
                SettingsItem(
                    icon = Icons.Rounded.AccountCircle,
                    title = "Active Wallet",
                    subtitle = "Manage wallet-specific settings",
                    onClick = { navController.navigate(UnifiedRoute.WalletSettings) }
                )
            }

            // General Section
            SettingsSection(title = "General") {
                SettingsItem(
                    icon = Icons.Rounded.AttachMoney,
                    title = "Currency",
                    subtitle = selectedCurrency,
                    onClick = { showCurrencyPicker = true }
                )

                SettingsSwitchItem(
                    icon = Icons.Rounded.VisibilityOff,
                    title = "Privacy Mode",
                    subtitle = "Hide balances in app",
                    checked = privacyMode,
                    onCheckedChange = { viewModel.togglePrivacyMode(it) }
                )
            }

            SettingsSection(title = "Network") {
                SettingsSwitchItem(
                    icon = Icons.Rounded.Cloud,
                    title = "Testnet Mode",
                    subtitle = "Use test networks (Devnet)",
                    checked = testnetEnabled,
                    onCheckedChange = { viewModel.toggleTestnet(it) }
                )
            }

            // Security Section
            SettingsSection(title = "Security") {
                SettingsSwitchItem(
                    icon = Icons.Rounded.Fingerprint,
                    title = "Biometric Authentication",
                    subtitle = "Use fingerprint/face to unlock",
                    checked = biometricEnabled,
                    onCheckedChange = { viewModel.toggleBiometric(it) }
                )

                SettingsSwitchItem(
                    icon = Icons.Rounded.Lock,
                    title = "Auto-Lock",
                    subtitle = "Lock app after inactivity",
                    checked = autoLockEnabled,
                    enabled = biometricEnabled,
                    onCheckedChange = { viewModel.toggleAutoLock(it) }
                )

                if (autoLockEnabled) {
                    SettingsItem(
                        icon = Icons.Rounded.Timer,
                        title = "Lock Timeout",
                        subtitle = formatTimeout(autoLockTimeout),
                        onClick = { showTimeoutPicker = true }
                    )
                }
            }

            // Appearance Section
            SettingsSection(title = "Appearance") {
                SettingsItem(
                    icon = Icons.Rounded.Palette,
                    title = "Theme",
                    subtitle = theme,
                    onClick = { /* Show theme picker */ }
                )
            }

            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = "Version",
                    subtitle = viewModel.getAppVersion(),
                    onClick = { }
                )
                SettingsItem(
                    icon = Icons.Rounded.Help,
                    title = "Support",
                    subtitle = "Contact our support time anytime",
                    onClick = { /* Open support */ }
                )
                SettingsItem(
                    icon = Icons.Rounded.Description,
                    title = "Terms of Service",
                    subtitle = "Read terms of service",
                    onClick = { /* Open terms */ }
                )
                SettingsItem(
                    icon = Icons.Rounded.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Read our policy",
                    onClick = { /* Open privacy */ }
                )
            }
        }
    }

    // Dialogs
    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currentCurrency = selectedCurrency,
            onDismiss = { showCurrencyPicker = false },
            onSelect = { currency ->
                viewModel.updateCurrency(currency)
                showCurrencyPicker = false
            }
        )
    }

    if (showTimeoutPicker) {
        TimeoutPickerDialog(
            currentTimeout = autoLockTimeout.toIntOrNull() ?: 300,
            onDismiss = { showTimeoutPicker = false },
            onSelect = { timeout ->
                viewModel.setAutoLockTimeout(timeout)
                showTimeoutPicker = false
            }
        )
    }
}

// ========== 4. REUSABLE COMPONENTS ==========
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
    icon: ImageVector,
    title: String,
    subtitle: String,
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
                tint = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}
