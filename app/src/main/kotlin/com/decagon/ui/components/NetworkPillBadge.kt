package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.core.network.NetworkEnvironment
import com.decagon.domain.model.DecagonWallet
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Format currency values with proper symbols and formatting
 */
fun formatCurrency(amount: Double, currencyCode: String = "USD"): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.currency = Currency.getInstance(currencyCode.uppercase())
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
        format.format(amount)
    } catch (e: Exception) {
        // Fallback for unsupported currencies
        val symbol = when (currencyCode.lowercase()) {
            "usd" -> "$"
            "eur" -> "€"
            "gbp" -> "£"
            "ngn" -> "₦"
            else -> currencyCode.uppercase()
        }
        "$symbol%.2f".format(amount)
    }
}

/**
 * Network pill badge component for displaying current network
 */
@Composable
fun NetworkPillBadge(
    network: NetworkEnvironment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2A2A34).copy(alpha = 0.8f),
                        Color(0xFF1A1A24).copy(alpha = 0.9f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        when (network) {
                            NetworkEnvironment.MAINNET -> Color(0xFF14F195).copy(alpha = 0.4f)
                            NetworkEnvironment.DEVNET -> Color(0xFFFF6B6B).copy(alpha = 0.4f)
                            NetworkEnvironment.TESTNET -> Color(0xFFFFB84D).copy(alpha = 0.4f)
                        },
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = network.displayName,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                color = when (network) {
                    NetworkEnvironment.MAINNET -> Color(0xFF14F195)
                    NetworkEnvironment.DEVNET -> Color(0xFFFF6B6B)
                    NetworkEnvironment.TESTNET -> Color(0xFFFFB84D)
                }
            )
        )
    }
}

// Fixed modal components without blur



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
private fun WalletOptionItem(
    wallet: DecagonWallet,
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