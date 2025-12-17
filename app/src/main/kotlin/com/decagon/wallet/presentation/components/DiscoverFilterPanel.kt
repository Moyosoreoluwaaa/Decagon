package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.viewmodel.SortType

@Composable
fun DiscoverFilterPanel(
    sortType: SortType,
    onSortTypeChanged: (SortType) -> Unit,
    showOnlyPositive: Boolean,
    onTogglePositiveOnly: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.Padding.standard),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimensions.Padding.standard),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            // Sort by label
            Text(
                "Sort by",
                style = AppTypography.titleMedium,
                color = AppColors.TextPrimary
            )

            // Sort chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
            ) {
                SortChip(
                    text = "Rank",
                    selected = sortType == SortType.RANK,
                    onClick = { onSortTypeChanged(SortType.RANK) }
                )
                SortChip(
                    text = "Name",
                    selected = sortType == SortType.NAME,
                    onClick = { onSortTypeChanged(SortType.NAME) }
                )
                SortChip(
                    text = "Price",
                    selected = sortType == SortType.PRICE,
                    onClick = { onSortTypeChanged(SortType.PRICE) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
            ) {
                SortChip(
                    text = "Change",
                    selected = sortType == SortType.CHANGE,
                    onClick = { onSortTypeChanged(SortType.CHANGE) }
                )
                SortChip(
                    text = "Market Cap",
                    selected = sortType == SortType.MARKET_CAP,
                    onClick = { onSortTypeChanged(SortType.MARKET_CAP) }
                )
            }

            // Positive-only filter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
            ) {
                Checkbox(
                    checked = showOnlyPositive,
                    onCheckedChange = { onTogglePositiveOnly() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColors.Solana
                    )
                )
                Text(
                    "Show only positive change",
                    style = AppTypography.bodyMedium,
                    color = AppColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun SortChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text)
                if (selected) {
                    Icon(
                        Icons.AutoMirrored.Filled.Sort,
                        contentDescription = "Sorted",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AppColors.Solana.copy(alpha = 0.2f),
            selectedLabelColor = AppColors.Solana,
            containerColor = AppColors.SurfaceHighlight,
            labelColor = AppColors.TextSecondary
        ),
        modifier = modifier
    )
}