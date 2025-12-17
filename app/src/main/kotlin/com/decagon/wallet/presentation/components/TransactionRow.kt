package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters

@Composable
internal fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    formatType: (TransactionType) -> String,
    getIcon: (TransactionType) -> String,
    getStatusColor: (TransactionStatus) -> Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Dimensions.Padding.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(Dimensions.Avatar.medium)
                    .clip(CircleShape)
                    .background(AppColors.SurfaceHighlight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getIcon(transaction.type),
                    style = AppTypography.bodyMedium
                )
            }

            Column {
                Text(
                    formatType(transaction.type),
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.extraSmall)
                ) {
                    Text(
                        UiFormatters.formatRelativeTime(transaction.timestamp),
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                    Text("•", style = AppTypography.bodySmall, color = AppColors.TextSecondary)
                    Text(
                        transaction.tokenSymbol,
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                "${if (transaction.type == TransactionType.SEND) "-" else "+"}${transaction.amount}",
                style = AppTypography.titleSmall,
                color = AppColors.TextPrimary
            )
            TransactionStatusBadge(
                status = transaction.status,
                color = getStatusColor(transaction.status)
            )
        }
    }
}


