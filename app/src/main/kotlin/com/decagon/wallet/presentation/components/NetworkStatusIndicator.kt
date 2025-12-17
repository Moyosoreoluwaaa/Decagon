package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.octane.wallet.domain.models.NetworkHealth
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Network status indicator (V1.9 feature).
 * Shows RPC connection health.
 */
@Composable
fun NetworkStatusIndicator(
    networkHealth: NetworkHealth,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, color, label) = when (networkHealth) {
        is NetworkHealth.Healthy -> Triple(
            Icons.Rounded.CheckCircle,
            AppColors.Success,
            "Synced"
        )
        is NetworkHealth.Slow -> Triple(
            Icons.Rounded.Warning,
            AppColors.Warning,
            "Slow"
        )
        is NetworkHealth.Down, is NetworkHealth.Degraded -> Triple(
            Icons.Rounded.Error,
            AppColors.Error,
            "Error"
        )
        NetworkHealth.Offline -> Triple(
            Icons.Rounded.Error,
            AppColors.Neutral,
            "Offline"
        )
        NetworkHealth.Unknown -> Triple(
            Icons.Rounded.Warning,
            AppColors.Neutral,
            "Unknown"
        )
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
        modifier = modifier
            .clip(CircleShape)
            .background(AppColors.Surface)
            .clickable(onClick = onClick)
            .padding(
                horizontal = Dimensions.Padding.medium,
                vertical = Dimensions.Padding.small
            )
    ) {
        // Status Dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        // Label
        Text(
            text = label,
            style = AppTypography.labelSmall,
            color = AppColors.TextSecondary
        )
    }
}