package com.decagon.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.decagon.ui.theme.AppColors
import com.decagon.ui.theme.AppTypography
import com.decagon.ui.screen.discover.SortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarWithFilter(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onResetSearch: () -> Unit,
    onToggleFilters: () -> Unit,
    showFilters: Boolean,
    modifier: Modifier = Modifier
) {
    SearchBar(
        query = searchQuery,
        onQueryChange = onQueryChange,
        onSearch = { },
        active = false,
        onActiveChange = { },
        placeholder = { Text("Search...", style = AppTypography.bodyLarge) },
        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = AppColors.TextSecondary) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = onResetSearch) {
                        Icon(Icons.Rounded.Clear, null, tint = AppColors.TextSecondary)
                    }
                }
                IconButton(onClick = onToggleFilters) {
                    val rotation by animateFloatAsState(
                        targetValue = if (showFilters) 180f else 0f,
                        label = "filter_rotation"
                    )
                    Icon(
                        Icons.Rounded.FilterList,
                        null,
                        tint = if (showFilters) AppColors.Solana else AppColors.TextSecondary,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }
        },
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        // fillMaxWidth is standard, but we control margins in the parent screen
        modifier = modifier.fillMaxWidth()
    ) {}
}

@Composable
fun FilterPanel(
    sortType: SortType,
    onSortTypeChanged: (SortType) -> Unit,
    showOnlyPositive: Boolean,
    onTogglePositiveOnly: () -> Unit,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Filters & Sorting", style = AppTypography.titleMedium)
                TextButton(onClick = onResetFilters) {
                    Text("Reset", color = AppColors.Solana)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = sortType == SortType.RANK,
                    onClick = { onSortTypeChanged(SortType.RANK) },
                    label = { Text("Rank") }
                )
                FilterChip(
                    selected = sortType == SortType.PRICE,
                    onClick = { onSortTypeChanged(SortType.PRICE) },
                    label = { Text("Price") }
                )
                FilterChip(
                    selected = sortType == SortType.CHANGE,
                    onClick = { onSortTypeChanged(SortType.CHANGE) },
                    label = { Text("Change") }
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = showOnlyPositive,
                    onCheckedChange = { onTogglePositiveOnly() }
                )
                Text("Show only positive change")
            }
        }
    }
}