package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.core.network.NetworkEnvironment
import com.decagon.domain.model.DecagonWallet
import kotlin.collections.forEach


@Composable
fun NetworkSelectorModal(
    currentNetwork: NetworkEnvironment,
    onNetworkSelect: (NetworkEnvironment) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(340.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E1E28),
                            Color(0xFF12121A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9945FF).copy(alpha = 0.35f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(28.dp)
                .clickable(enabled = false) {} // Prevent click-through
        ) {
            Text(
                text = "Select Network",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            NetworkEnvironment.values().forEach { network ->
                NetworkOption(
                    network = network,
                    isActive = network == currentNetwork,
                    onClick = {
                        onNetworkSelect(network)
                        onDismiss()
                    }
                )
                if (network != NetworkEnvironment.values().last()) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun NetworkOption(
    network: NetworkEnvironment,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isActive) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9945FF).copy(alpha = 0.2f),
                            Color(0xFF9945FF).copy(alpha = 0.1f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A34).copy(alpha = 0.3f),
                            Color(0xFF1A1A24).copy(alpha = 0.5f)
                        )
                    )
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Network indicator dot
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) Color(0xFF14F195) else Color(0xFF3A3A44)
                    )
            )

            Text(
                text = network.displayName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isActive) Color.White else Color(0xFFB4B4C6)
                )
            )
        }

        if (isActive) {
            Text(
                text = "(Active)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF14F195)
                )
            )
        }
    }
}

@Composable
fun WalletSwitcherModal(
    wallets: List<DecagonWallet>,
    activeWalletId: String,
    onWalletSelect: (String) -> Unit,
    onAddWallet: () -> Unit,
    onSettings: () -> Unit,
    onLogOut: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, top = 72.dp)
                .width(280.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1E28),
                            Color(0xFF12121A)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF9945FF).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
                .clickable(enabled = false) {}
        ) {
            Text(
                text = "Wallet Switcher",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Wallet list
            wallets.forEach { wallet ->
                WalletOptionItem(
                    wallet = wallet,
                    isActive = wallet.id == activeWalletId,
                    onClick = {
                        onWalletSelect(wallet.id)
                        onDismiss()
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            Divider(
                color = Color(0xFF3A3A44),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Actions
            ActionMenuItem(
                label = "Add New Wallet",
                onClick = {
                    onAddWallet()
                    onDismiss()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionMenuItem(
                label = "Settings",
                onClick = {
                    onSettings()
                    onDismiss()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            ActionMenuItem(
                label = "Log Out",
                isDestructive = true,
                onClick = {
                    onLogOut()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun WalletOptionItem(
    wallet: com.decagon.domain.model.DecagonWallet,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) {
                    Color(0xFF9945FF).copy(alpha = 0.15f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF2A2A34)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = wallet.name.take(1).uppercase(),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                )
                if (isActive) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF14F195))
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionMenuItem(
    label: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge.copy(
            color = if (isDestructive) Color(0xFFFF6B6B) else Color(0xFFB4B4C6)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    )
}