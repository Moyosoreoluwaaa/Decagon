package com.decagon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.decagon.core.network.FeatureFlags
import com.decagon.core.network.NetworkManager
import com.decagon.domain.model.DecagonWallet
import org.koin.compose.koinInject

@Composable
fun DecagonQuickActions(
    wallet: DecagonWallet,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onNavigateToSwap: () -> Unit,
    networkManager: NetworkManager = koinInject()  // ← NEW
) {
    val currentNetwork by networkManager.currentNetwork.collectAsState()
    val canBuy = FeatureFlags.isBuyEnabled(currentNetwork)
    val canSwap = FeatureFlags.isSwapEnabled(currentNetwork)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionButton(
            icon = Icons.Default.Send,
            label = "Send",
            enabled = true,
            onClick = onSendClick
        )

        QuickActionButton(
            icon = Icons.Default.Download,
            label = "Receive",
            enabled = true,
            onClick = onReceiveClick
        )

        QuickActionButton(
            icon = Icons.Default.ShoppingCart,
            label = "Buy",
            enabled = canBuy,
            onClick = if (canBuy) onBuyClick else { {} },
            disabledReason = if (!canBuy) "Mainnet only" else null
        )

        QuickActionButton(
            icon = Icons.Default.SwapHoriz,
            label = "Swap",
            enabled = canSwap,
            onClick = if (canSwap) onNavigateToSwap else { {} },
            disabledReason = if (!canSwap) "Mainnet only" else null
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    disabledReason: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            enabled = enabled
        ) {
            Icon(icon, contentDescription = label)
        }

        Text(
            text = if (!enabled && disabledReason != null) disabledReason else label,
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}