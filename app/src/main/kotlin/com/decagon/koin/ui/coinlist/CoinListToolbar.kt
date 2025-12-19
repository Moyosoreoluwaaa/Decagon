package com.koin.ui.coinlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.koin.domain.model.Coin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinListToolbar(
    searchQuery: String,
    isSearchActive: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onActiveChange: (Boolean) -> Unit,
    onResetSearch: () -> Unit,
    onToggleFilters: () -> Unit,
    showFiltersActive: Boolean,
    filterIconRotation: Float,
    onResetAllFilters: () -> Unit,
    showResetFiltersButton: Boolean,
    filteredCoins: List<Coin>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // DockedSearchBar
        DockedSearchBar(
            query = searchQuery,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            active = isSearchActive,
            onActiveChange = onActiveChange,
            placeholder = { Text("Search coins...", style = MaterialTheme.typography.bodyLarge) },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                Row {
                    // Reset Search Button (inside search bar, visible if query exists)
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn(animationSpec = tween(200)) + expandHorizontally(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(200)) + shrinkHorizontally(animationSpec = tween(200))
                    ) {
                        IconButton(onClick = onResetSearch) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    // Filter Button (inside search bar)
                    IconButton(onClick = onToggleFilters) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter coins",
                            modifier = Modifier.rotate(filterIconRotation),
                            tint = if (showFiltersActive)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            colors = SearchBarDefaults.colors(
                dividerColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier.weight(1f)
        ) {
            // Search suggestions/recent searches content
            if (searchQuery.isEmpty()) {
                Text(
                    text = "Start typing to search",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val suggestions = filteredCoins.filter { it.name.contains(searchQuery, ignoreCase = true) || it.symbol.contains(searchQuery, ignoreCase = true) }
                if (suggestions.isNotEmpty()) {
                    suggestions.forEach { coin ->
                        ListItem(
                            headlineContent = { Text(coin.name) },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.clickable {
                                onQueryChange(coin.name)
                                onSearch(coin.name)
                                onActiveChange(false)
                            }
                        )
                    }
                } else {
                    Text(
                        text = "No matching coins found",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // --- Buttons OUTSIDE the Search Bar (dynamic visibility) ---

        // Reset All Filters Button - visible when search is INACTIVE AND filters are applied
        AnimatedVisibility(
            visible = !isSearchActive && showResetFiltersButton,
            enter = fadeIn(animationSpec = tween(250, delayMillis = 50)) +
                    slideInHorizontally(
                        animationSpec = tween(250, delayMillis = 50),
                        initialOffsetX = { fullWidth -> fullWidth / 2 }
                    ) +
                    scaleIn(animationSpec = tween(250, delayMillis = 50), initialScale = 0.8f),
            exit = fadeOut(animationSpec = tween(250)) +
                    slideOutHorizontally(
                        animationSpec = tween(250),
                        targetOffsetX = { fullWidth -> fullWidth / 2 }
                    ) +
                    scaleOut(animationSpec = tween(250), targetScale = 0.8f)
        ) {
            IconButton(
                onClick = onResetAllFilters,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterListOff,
                    contentDescription = "Reset all filters",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}