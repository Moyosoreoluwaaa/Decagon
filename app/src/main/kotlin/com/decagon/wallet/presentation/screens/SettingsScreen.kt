package com.octane.wallet.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DeveloperMode
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Support
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.octane.wallet.presentation.components.MetallicCard
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.viewmodel.SettingsViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val privacyMode by viewModel.privacyMode.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimensions.Padding.standard),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large)
        ) {
            // General Section
            item {
                SectionHeader("General")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                        SettingRow(
                            icon = Icons.Rounded.AttachMoney,
                            title = "Currency",
                            subtitle = selectedCurrency,
                            onClick = { /* TODO: Show currency picker */ }
                        )

                        Divider()

                        SettingRowWithToggle(
                            icon = Icons.Rounded.Visibility,
                            title = "Privacy Mode",
                            subtitle = "Hide balances",
                            checked = privacyMode,
                            onCheckedChange = viewModel::togglePrivacyMode
                        )
                    }
                }
            }

            // Security Section
            item {
                SectionHeader("Security")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                        SettingRowWithToggle(
                            icon = Icons.Rounded.Fingerprint,
                            title = "Biometric Authentication",
                            subtitle = if (settingsState.biometricAvailable)
                                "Secure with fingerprint" else "Not available",
                            checked = settingsState.biometricEnabled,
                            onCheckedChange = viewModel::toggleBiometric,
                            enabled = settingsState.biometricAvailable
                        )

                        Divider()

                        SettingRow(
                            icon = Icons.Rounded.Key,
                            title = "Show Seed Phrase",
                            subtitle = "Requires authentication",
                            onClick = viewModel::requestShowSeedPhrase
                        )

                        Divider()

                        SettingRow(
                            icon = Icons.Rounded.Lock,
                            title = "Auto-Lock",
                            subtitle = "${settingsState.autoLockTimeout / 60} minutes",
                            onClick = { /* TODO: Show auto-lock picker */ }
                        )
                    }
                }
            }

            // Network Section
            item {
                SectionHeader("Network")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                        SettingRowWithToggle(
                            icon = Icons.Rounded.DeveloperMode,
                            title = "Testnet Mode",
                            subtitle = "Use Solana Devnet",
                            checked = settingsState.testnetEnabled,
                            onCheckedChange = viewModel::toggleTestnet
                        )

                        Divider()

                        SettingRow(
                            icon = Icons.Rounded.Dns,
                            title = "RPC Endpoint",
                            subtitle = "Mainnet (Default)",
                            onClick = { /* TODO: Show RPC picker */ }
                        )
                    }
                }
            }

            // Appearance Section
            item {
                SectionHeader("Appearance")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                        SettingRow(
                            icon = Icons.Rounded.Palette,
                            title = "Theme",
                            subtitle = settingsState.theme.name.lowercase().capitalize(),
                            onClick = { /* TODO: Show theme picker */ }
                        )

                        Divider()

                        SettingRow(
                            icon = Icons.Rounded.Language,
                            title = "Language",
                            subtitle = "English",
                            onClick = { /* TODO: Show language picker */ }
                        )
                    }
                }
            }

            // About Section
            item {
                SectionHeader("About")
            }

            item {
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                        SettingRow(
                            icon = Icons.Rounded.Info,
                            title = "Version",
                            subtitle = viewModel.getAppVersion(),
                            onClick = {}
                        )

                        Divider()

                        SettingRow(
                            icon = Icons.Rounded.Support,
                            title = "Support",
                            subtitle = "Get help",
                            onClick = viewModel::openSupport
                        )

                        Divider()

                        SettingRow(
                            icon = Icons.Rounded.Description,
                            title = "Terms of Service",
                            subtitle = "Legal information",
                            onClick = viewModel::openTerms
                        )

                        Divider()

                        SettingRow(
                            icon = Icons.Rounded.Policy,
                            title = "Privacy Policy",
                            subtitle = "How we protect your data",
                            onClick = viewModel::openPrivacyPolicy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = AppTypography.labelLarge,
        color = AppColors.TextSecondary,
        modifier = Modifier.padding(
            horizontal = Dimensions.Padding.small,
            vertical = Dimensions.Spacing.small
        )
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    MetallicCard(
        modifier = Modifier.fillMaxWidth(),
        content = { content() }
    )
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = Dimensions.Spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) AppColors.TextPrimary else AppColors.TextTertiary,
                modifier = Modifier.size(Dimensions.IconSize.medium)
            )

            Column {
                Text(
                    title,
                    style = AppTypography.bodyMedium,
                    color = if (enabled) AppColors.TextPrimary else AppColors.TextTertiary
                )
                Text(
                    subtitle,
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextSecondary,
            modifier = Modifier.size(Dimensions.IconSize.medium)
        )
    }
}

@Composable
private fun SettingRowWithToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) AppColors.TextPrimary else AppColors.TextTertiary,
                modifier = Modifier.size(Dimensions.IconSize.medium)
            )

            Column {
                Text(
                    title,
                    style = AppTypography.bodyMedium,
                    color = if (enabled) AppColors.TextPrimary else AppColors.TextTertiary
                )
                Text(
                    subtitle,
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }

        IndustrialSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun Divider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = Dimensions.Spacing.small),
        color = AppColors.BorderDefault.copy(alpha = 0.3f)
    )
}

@Composable
private fun IndustrialSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = AppColors.Primary,
            checkedTrackColor = AppColors.Primary.copy(alpha = 0.5f),
            uncheckedThumbColor = AppColors.TextSecondary,
            uncheckedTrackColor = AppColors.SurfaceHighlight
        )
    )
}