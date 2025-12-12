// ============================================================================
// FILE: ui/screen/onramp/DecagonProviderSelectionSheet.kt
// PURPOSE: Manual provider selection with regional availability information
// ============================================================================

package com.decagon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.core.config.OnRampProviderType
import com.decagon.domain.model.DecagonWallet

/**
 * Provider selection bottom sheet.
 * 
 * Features:
 * - Shows available providers with regional information
 * - Displays provider capabilities (currencies, payment methods)
 * - Warns about regional restrictions
 * - Allows manual provider selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonProviderSelectionSheet(
    wallet: DecagonWallet,
    availableProviders: List<ProviderInfo>,
    currentProvider: OnRampProviderType?,
    onProviderSelected: (OnRampProviderType) -> Unit,
    onDismiss: () -> Unit
) {
    var showProviderDetails by remember { mutableStateOf<ProviderInfo?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Payment Provider",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Choose how you'd like to add funds to your wallet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (availableProviders.isEmpty()) {
                EmptyProvidersView()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableProviders) { provider ->
                        ProviderCard(
                            provider = provider,
                            isSelected = provider.type == currentProvider,
                            onClick = {
                                if (provider.isAvailableInRegion) {
                                    onProviderSelected(provider.type)
                                    onDismiss()
                                } else {
                                    showProviderDetails = provider
                                }
                            },
                            onInfoClick = { showProviderDetails = provider }
                        )
                    }
                }
            }
        }
    }

    // Provider details dialog
    showProviderDetails?.let { provider ->
        ProviderDetailsDialog(
            provider = provider,
            onDismiss = { showProviderDetails = null }
        )
    }
}

@Composable
private fun ProviderCard(
    provider: ProviderInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                enabled = provider.isAvailableInRegion,
                onClick = onClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                !provider.isAvailableInRegion -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = provider.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )

                    if (provider.isRecommended && provider.isAvailableInRegion) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "Recommended",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when {
                        !provider.isAvailableInRegion -> "Coming soon to your region"
                        else -> provider.description
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        !provider.isAvailableInRegion -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                if (provider.isAvailableInRegion) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProviderTag(provider.paymentMethods)
                        if (provider.supportsMobileWallet) {
                            ProviderTag("Mobile Money")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onInfoClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Provider details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (!provider.isAvailableInRegion) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Unavailable",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun ProviderTag(text: String) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyProvidersView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No providers available",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Please configure at least one payment provider in settings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProviderDetailsDialog(
    provider: ProviderInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(provider.displayName)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = provider.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Divider()

                DetailRow("Payment Methods", provider.paymentMethods)
                DetailRow("Supported Currencies", provider.supportedCurrencies)
                DetailRow("Processing Time", provider.processingTime)
                DetailRow("Fees", provider.feeStructure)

                if (provider.supportsMobileWallet) {
                    DetailRow("Mobile Money", "Supported")
                }

                if (!provider.isAvailableInRegion) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = provider.regionalAvailabilityMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Provider information for UI display.
 */
data class ProviderInfo(
    val type: OnRampProviderType,
    val displayName: String,
    val description: String,
    val isAvailableInRegion: Boolean,
    val regionalAvailabilityMessage: String,
    val isRecommended: Boolean,
    val paymentMethods: String,
    val supportedCurrencies: String,
    val processingTime: String,
    val feeStructure: String,
    val supportsMobileWallet: Boolean
)