package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.DecagonWallet


@Composable
fun DecagonQuickActionsBar(
    wallet: DecagonWallet,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        // This ensures the gap between the buttons is perfectly consistent
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            icon = Icons.AutoMirrored.Filled.Send,
            label = "Send",
            enabled = !wallet.isViewOnly,
            onClick = onSendClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Rounded.Download,
            label = "Receive",
            onClick = onReceiveClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Rounded.ShoppingCart,
            label = "Buy",
            onClick = onBuyClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Rounded.SwapHoriz,
            label = "Swap",
            enabled = !wallet.isViewOnly,
            onClick = onSwapClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            // 1. Define the shape
            .clip(RoundedCornerShape(16.dp))
            // 2. Apply background to the individual button
            .background(MaterialTheme.colorScheme.surfaceContainer)
            // 3. Clickable must be after background/clip for correct ripple behavior
            .clickable(enabled = enabled, onClick = onClick)
            // 4. Inner padding to give the content room inside the background
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp)),
//                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(25.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}