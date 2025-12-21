package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ChartActionGrid(
    onReceive: () -> Unit,
    onCashBuy: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ChartActionButton("Receive", Icons.Rounded.QrCode, onReceive)
        ChartActionButton("Cash Buy", Icons.Rounded.AttachMoney, onCashBuy)
        ChartActionButton("Share", Icons.Rounded.IosShare, onShare)
        ChartActionButton("More", Icons.Rounded.MoreHoriz, onMore)
    }
}