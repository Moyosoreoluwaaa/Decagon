package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.wallet.core.util.LoadingState
import com.octane.wallet.presentation.components.ManageTokenRow
import com.octane.wallet.presentation.components.SearchInput
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.viewmodel.ManageTokensViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageTokensScreen(
    viewModel: ManageTokensViewModel = koinViewModel(),
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredAssets by viewModel.filteredAssets.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Tokens") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            SearchInput(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                placeholder = "Search tokens...",
                modifier = Modifier.padding(Dimensions.Padding.standard)
            )
            
            // Token List
            when (val state = filteredAssets) {
                is LoadingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                }
                
                is LoadingState.Success -> {
                    if (state.data.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(
                                "No tokens found",
                                style = AppTypography.bodyMedium,
                                color = AppColors.TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(Dimensions.Padding.standard),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                        ) {
                            items(state.data) { asset ->
                                ManageTokenRow(
                                    symbol = asset.symbol,
                                    name = asset.name,
                                    balance = "%.4f".format(asset.balance),
                                    isEnabled = !asset.isHidden,
                                    iconColor = Color.DarkGray,
                                    onToggle = { enabled ->
                                        viewModel.onToggleVisibility(asset.id, !enabled)
                                    }
                                )
                            }
                        }
                    }
                }
                
                is LoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                        ) {
                            Text(
                                "Failed to load tokens",
                                style = AppTypography.bodyMedium,
                                color = AppColors.Error
                            )
                            Text(
                                state.message,
                                style = AppTypography.bodySmall,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                }
                
                else -> {}
            }
        }
    }
}