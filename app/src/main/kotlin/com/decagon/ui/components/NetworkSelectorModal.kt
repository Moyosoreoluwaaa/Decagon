package com.decagon.ui.components

import android.graphics.Color.alpha
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.decagon.core.network.NetworkEnvironment
import com.decagon.domain.model.DecagonWallet
import com.decagon.util.ContainerShape
import com.decagon.util.ItemShape
import com.decagon.util.SuccessGreen
import com.octane.wallet.presentation.theme.AppTypography

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
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(250.dp)
                .clip(ContainerShape)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
                .clickable(enabled = false) {}
        ) {
            Text(
                text = "Select Network",
                style = AppTypography.titleLarge,
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                NetworkEnvironment.entries.forEach { network ->
                    NetworkOption(
                        network = network,
                        isActive = network == currentNetwork,
                        onClick = {
                            onNetworkSelect(network)
                            onDismiss()
                        }
                    )
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
            .clip(ItemShape)
            .background(
                if (isActive) MaterialTheme.colorScheme.surfaceVariant
                else MaterialTheme.colorScheme.background
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isActive) SuccessGreen else MaterialTheme.colorScheme.outline)
            )
            Text(
                text = network.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isActive) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isActive) {
            Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp), tint = SuccessGreen)
        }
    }
}


@Composable
internal fun SendForm(onSend: (String, Double) -> Unit) {
    var toAddress by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AppTextField(
            value = toAddress,
            onValueChange = { toAddress = it },
            label = "Recipient Address",
            placeholder = "Solana address",
            trailingIcon = { DecagonQrScanner(onAddressScanned = { toAddress = it }) }
        )

        AppTextField(
            value = amount,
            onValueChange = { amount = it },
            label = "Amount (SOL)",
            placeholder = "0.0",
            keyboardType = KeyboardType.Decimal
        )

        Button(
            onClick = { onSend(toAddress, amount.toDoubleOrNull() ?: 0.0) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = ItemShape,
            enabled = toAddress.isNotBlank() && amount.toDoubleOrNull() != null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("Send", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, modifier = Modifier.alpha(0.5f)) },
        trailingIcon = trailingIcon,
        modifier = Modifier.fillMaxWidth(),
        shape = ItemShape,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        singleLine = true
    )
}

@Composable
internal fun HeaderRow(title: String, onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        IconButton(onClick = onDismiss) {
            Icon(Icons.Rounded.Close, "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun BottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = 12.dp)
            .size(width = 40.dp, height = 4.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
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
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .padding(start = 16.dp, top = 72.dp)
                .width(200.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(16.dp)
                .clickable(enabled = false) {}
        ) {
            Text(
                text = "Wallet Switcher",
                style = AppTypography.titleLarge,
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
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
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
                        fontWeight = FontWeight.Medium
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
            color = if (isDestructive) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp)
    )
}