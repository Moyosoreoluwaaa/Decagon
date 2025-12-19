package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.decagon.core.network.NetworkEnvironment
import com.decagon.domain.model.DecagonWallet

@Composable
fun FloatingTopBar(
    wallet: DecagonWallet,
    currentNetwork: NetworkEnvironment,
    onProfileClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile avatar with glass effect
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF1A1A24),
                            Color(0xFF0F0E13)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9945FF).copy(alpha = 0.3f),
                            Color(0xFF14F195).copy(alpha = 0.2f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = wallet.name.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        // Network pill badge
        NetworkPillBadge(
            network = currentNetwork,
            onClick = onNetworkClick
        )

        // Notification icon with glow - FIXED
        Box(
            modifier = Modifier
                .size(44.dp)
                .drawBehind {
                    // Subtle purple glow behind icon
                    drawCircle(
                        color = Color(0xFF9945FF).copy(alpha = 0.15f),
                        radius = 28.dp.toPx()
                    )
                }
        ) {
            IconButton(
                onClick = onNotificationClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFFB4B4C6)
                )
            }
        }
    }
}