package com.decagon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.DecagonWallet

// :ui:components/DecagonQuickActions.kt
@Composable
fun DecagonQuickActions(
    wallet: DecagonWallet,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.AutoMirrored.Filled.Send,
            label = "Send",
            onClick = onSendClick,
            modifier = Modifier.weight(1f)
        )
        
        QuickActionButton(
            icon = Icons.AutoMirrored.Filled.CallReceived,
            label = "Receive",
            onClick = onReceiveClick,
            modifier = Modifier.weight(1f)
        )
        
        // Later versions:
        // QuickActionButton(icon = Icons.Default.SwapHoriz, label = "Swap", ...)
        // QuickActionButton(icon = Icons.Default.ShoppingCart, label = "Buy", ...)
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}