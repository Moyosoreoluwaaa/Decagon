package com.decagon.ui.screen.all

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wallet.core.util.LoadingState
import com.octane.wallet.presentation.components.*
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllDAppsScreen(
    viewModel: AllDAppsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToDAppBrowser: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val dapps by viewModel.allDApps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val scrollState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All DApps", style = AppTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = AppColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Surface
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
                placeholder = "Search dApps...",
                modifier = Modifier.padding(Dimensions.Padding.standard)
            )
            
            // DApps List
            when (dapps) {
                is LoadingState.Loading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Dimensions.Padding.standard),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                    ) {
                        items(20) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .shimmerEffect()
                            )
                        }
                    }
                }
                
                is LoadingState.Success -> {
                    val dappList = (dapps as LoadingState.Success).data
                    
                    if (dappList.isEmpty()) {
                        EmptyState(
                            message = if (searchQuery.isNotBlank()) 
                                "No dApps found" 
                            else 
                                "No dApps available"
                        )
                    } else {
                        LazyColumn(
                            state = scrollState,
                            contentPadding = PaddingValues(Dimensions.Padding.standard),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                        ) {
                            items(dappList) { dapp ->
                                SiteRow(
                                    rank = dappList.indexOf(dapp) + 1,
                                    name = dapp.name,
                                    category = dapp.category.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    logoUrl = dapp.logoUrl,
                                    onClick = {
                                        Timber.d("🎯 DApp clicked: ${dapp.name}")
                                        onNavigateToDAppBrowser(dapp.url, dapp.name)
                                    }
                                )
                            }
                            
                            // Footer
                            item {
                                Text(
                                    "Showing ${dappList.size} dApps",
                                    style = AppTypography.bodySmall,
                                    color = AppColors.TextSecondary,
                                    modifier = Modifier.padding(vertical = Dimensions.Spacing.medium)
                                )
                            }
                        }
                    }
                }
                
                is LoadingState.Error -> {
                    ErrorScreen(
                        message = (dapps as LoadingState.Error).message,
                        onRetry = viewModel::refreshDApps
                    )
                }
                
                else -> {}
            }
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.Padding.large),
        contentAlignment = Alignment.Center
    ) {
        Text(
            message,
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}