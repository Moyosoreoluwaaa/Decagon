package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.DecagonWallet

// Create new file: :ui:components/DecagonQuickActionsBar.kt

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
            .padding(horizontal = 32.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF1A1A24).copy(alpha = 0.6f),
                        Color(0xFF0F0E13).copy(alpha = 0.8f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF9945FF).copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuickActionButton(
            icon = Icons.AutoMirrored.Filled.Send,
            label = "Send",
            enabled = !wallet.isViewOnly,
            onClick = onSendClick
        )
        QuickActionButton(
            icon = Icons.Default.Download,
            label = "Receive",
            onClick = onReceiveClick
        )
        QuickActionButton(
            icon = Icons.Default.ShoppingCart,
            label = "Buy",
            onClick = onBuyClick
        )
        QuickActionButton(
            icon = Icons.Default.SwapHoriz,
            label = "Swap",
            enabled = !wallet.isViewOnly,
            onClick = onSwapClick
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF2A2A34),
                            Color(0xFF1A1A24)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF9945FF).copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = Color(0xFFB4B4C6)
            )
        )
    }
}