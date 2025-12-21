package com.octane.wallet.presentation.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.viewmodel.SearchSuggestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverToolbar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onResetSearch: () -> Unit,
    onToggleFilters: () -> Unit,
    showFilters: Boolean,
    onResetFilters: () -> Unit,
    showResetButton: Boolean,
    searchSuggestions: List<SearchSuggestion>,
    onSuggestionClick: (SearchSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = Dimensions.Padding.standard,
                vertical = Dimensions.Spacing.medium
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // DockedSearchBar with suggestions
            DockedSearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange,
                onSearch = { isSearchActive = false },
                active = isSearchActive,
                onActiveChange = { isSearchActive = it },
                placeholder = {
                    Text(
                        "Sites, tokens, URL...",
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        tint = AppColors.TextSecondary
                    )
                },
                trailingIcon = {
                    Row {
                        // Clear search button
                        AnimatedVisibility(
                            visible = searchQuery.isNotEmpty(),
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            IconButton(onClick = onResetSearch) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear search",
                                    tint = AppColors.TextSecondary
                                )
                            }
                        }

                        // Filter button
                        IconButton(onClick = onToggleFilters) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (showFilters) AppColors.Solana else AppColors.TextSecondary
                            )
                        }
                    }
                },
                colors = SearchBarDefaults.colors(
                    containerColor = AppColors.Surface,
                    dividerColor = AppColors.SurfaceHighlight
                ),
                modifier = Modifier.weight(1f)
            ) {
                // Search suggestions dropdown
                if (searchQuery.isBlank()) {
                    Text(
                        "Start typing to search",
                        modifier = Modifier.padding(Dimensions.Padding.standard),
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else if (searchSuggestions.isNotEmpty()) {
                    searchSuggestions.forEach { suggestion ->
                        ListItem(
                            headlineContent = {
                                Text(
                                    suggestion.displayName,
                                    color = AppColors.TextPrimary
                                )
                            },
                            supportingContent = {
                                Text(
                                    suggestion.subtitle,
                                    color = AppColors.TextSecondary
                                )
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = AppColors.TextSecondary
                                )
                            },
                            modifier = Modifier.clickable {
                                onSuggestionClick(suggestion)
                                isSearchActive = false
                            }
                        )
                    }
                } else {
                    Text(
                        "No matching results",
                        modifier = Modifier.padding(Dimensions.Padding.standard),
                        color = AppColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Reset filters button (outside search bar)
            AnimatedVisibility(
                visible = !isSearchActive && showResetButton,
                enter = fadeIn() + slideInHorizontally() + scaleIn(),
                exit = fadeOut() + slideOutHorizontally() + scaleOut()
            ) {
                IconButton(onClick = onResetFilters) {
                    Icon(
                        Icons.Default.FilterListOff,
                        contentDescription = "Reset filters",
                        tint = AppColors.Solana
                    )
                }
            }
        }
    }
}