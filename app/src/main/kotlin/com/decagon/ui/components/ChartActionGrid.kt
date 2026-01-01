package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.QrCode
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Action grid for token/perp detail screens.
 * Shows 4 action buttons in a horizontal row.
 */
@Composable
internal fun ChartActionGrid(
    onReceive: () -> Unit,
    onCashBuy: () -> Unit,
    onShare: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        ChartActionButton(
            label = "Receive",
            icon = Icons.Rounded.QrCode,
            onClick = onReceive,
            modifier = Modifier.weight(1f)
        )
        ChartActionButton(
            label = "Buy",
            icon = Icons.Rounded.AttachMoney,
            onClick = onCashBuy,
            modifier = Modifier.weight(1f)
        )
        ChartActionButton(
            label = "Share",
            icon = Icons.Rounded.IosShare,
            onClick = onShare,
            modifier = Modifier.weight(1f)
        )
        ChartActionButton(
            label = "More",
            icon = Icons.Rounded.MoreHoriz,
            onClick = onMore,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual action button with icon and label.
 * Matches the design system from Discover/Perps screens.
 */
@Composable
private fun ChartActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hapticFeedback = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            style = AppTypography.labelSmall,
        )
    }
}