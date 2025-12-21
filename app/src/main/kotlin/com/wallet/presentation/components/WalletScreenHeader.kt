package com.octane.wallet.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun WalletScreenHeader(
    currentPage: Int,
    pendingCount: Int,
    onBack: () -> Unit,
    onTabClick: (Int) -> Unit,
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Column {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Padding.standard),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = AppColors.TextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (currentPage == 0) "Activity" else "Wallets",
                    style = AppTypography.headlineSmall,
                    color = AppColors.TextPrimary
                )
                if (currentPage == 0 && pendingCount > 0) {
                    Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AppColors.Warning)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "$pendingCount",
                            style = AppTypography.labelSmall,
                            color = Color.Black
                        )
                    }
                }
            }

            if (currentPage == 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            Icons.Rounded.FilterList,
                            contentDescription = "Filter",
                            tint = AppColors.TextPrimary
                        )
                    }
                    IconButton(onClick = onExportClick) {
                        Icon(
                            Icons.Rounded.FileDownload,
                            contentDescription = "Export",
                            tint = AppColors.TextPrimary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp)) // Balance for back button
            }
        }

        // Tab Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimensions.Padding.standard)
                .padding(bottom = Dimensions.Padding.standard),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
        ) {
            TabButton(
                text = "Activity",
                isSelected = currentPage == 0,
                onClick = { onTabClick(0) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                text = "Wallets",
                isSelected = currentPage == 1,
                onClick = { onTabClick(1) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}