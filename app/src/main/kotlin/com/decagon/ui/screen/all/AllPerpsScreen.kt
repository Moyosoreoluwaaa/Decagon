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
import androidx.compose.ui.graphics.Color
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
fun AllPerpsScreen(
    viewModel: AllPerpsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPerpDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val perps by viewModel.allPerps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val scrollState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Perpetuals", style = AppTypography.titleLarge) },
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
                placeholder = "Search perpetuals...",
                modifier = Modifier.padding(Dimensions.Padding.standard)
            )
            
            // Perps List
            when (perps) {
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
                    val perpList = (perps as LoadingState.Success).data
                    
                    if (perpList.isEmpty()) {
                        EmptyState(
                            message = if (searchQuery.isNotBlank()) 
                                "No perpetuals found" 
                            else 
                                "No perpetuals available"
                        )
                    } else {
                        LazyColumn(
                            state = scrollState,
                            contentPadding = PaddingValues(Dimensions.Padding.standard),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                        ) {
                            items(perpList) { perp ->
                                PerpRow(
                                    symbol = perp.symbol,
                                    name = perp.name,
                                    price = "$${perp.indexPrice}",
                                    changePercent = perp.priceChange24h,
                                    volume24h = perp.formattedOpenInterest,
                                    leverageMax = perp.leverage.replace("x", "").toIntOrNull() ?: 20,
                                    logoUrl = perp.logoUrl,
                                    fallbackIconColor = getTokenColor(perp.symbol.split("-").first()),
                                    onClick = {
                                        Timber.d("🎯 Perp clicked: ${perp.symbol}")
                                        onNavigateToPerpDetails(perp.symbol)
                                    }
                                )
                            }
                            
                            // Footer
                            item {
                                Text(
                                    "Showing ${perpList.size} perpetuals",
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
                        message = (perps as LoadingState.Error).message,
                        onRetry = viewModel::refreshPerps
                    )
                }
                
                else -> {}
            }
        }
    }
}

private fun getTokenColor(symbol: String): Color {
    return when (symbol.uppercase()) {
        "SOL" -> AppColors.Solana
        "BTC" -> AppColors.Bitcoin
        "ETH" -> AppColors.Ethereum
        "USDC" -> AppColors.USDC
        "USDT" -> AppColors.USDT
        else -> AppColors.TextSecondary
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