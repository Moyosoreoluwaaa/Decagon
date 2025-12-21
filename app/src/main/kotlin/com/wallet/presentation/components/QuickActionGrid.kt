package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Quick action grid for home screen.
 */
@Composable
fun QuickActionGrid(
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onSwap: () -> Unit,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Spacing.standard),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        MetallicButton(text = "Receive", icon = Icons.Rounded.QrCode, onClick = onReceive)
        MetallicButton(text = "Send", icon = Icons.AutoMirrored.Rounded.Send, onClick = onSend)
        MetallicButton(text = "Swap", icon = Icons.Rounded.SwapHoriz, onClick = onSwap)
        MetallicButton(text = "Buy", icon = Icons.Rounded.AttachMoney, onClick = onBuy)
    }
}

/**
 * Action grid for token detail screen.
 */
@Composable
fun ChartActionGrid(
    onReceive: () -> Unit,
    onCashBuy: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Dimensions.Spacing.standard),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChartActionButton("Receive", Icons.Rounded.QrCode, onReceive)
        ChartActionButton("Cash Buy", Icons.Rounded.AttachMoney, onCashBuy)
        ChartActionButton("Share", Icons.Rounded.IosShare, onShare)
        ChartActionButton("More", Icons.Rounded.MoreHoriz, onMore)
    }
}