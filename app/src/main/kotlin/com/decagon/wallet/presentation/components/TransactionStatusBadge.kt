package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

@Composable
internal fun TransactionStatusBadge(
    status: TransactionStatus,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(color.copy(alpha = 0.15f))
            .padding(
                horizontal = Dimensions.Padding.small,
                vertical = Dimensions.Padding.tiny
            )
    ) {
        Text(
            text = when (status) {
                TransactionStatus.CONFIRMED -> "Confirmed"
                TransactionStatus.PENDING -> "Pending"
                TransactionStatus.FAILED -> "Failed"
            },
            style = AppTypography.labelSmall,
            color = color
        )
    }
}