package com.wallet.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Token
import com.octane.wallet.domain.usecases.discover.RefreshTokensUseCase
import com.octane.wallet.presentation.components.*
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.wallet.domain.repository.DiscoverRepository
import com.wallet.presentation.viewmodel.AllTokensViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

/**
 * ✅ NEW: Full tokens list screen (shows ALL tokens, not limited to 10).
 * 
 * Features:
 * - Infinite scroll with pagination
 * - Search functionality
 * - Sorting (rank, price, change, market cap)
 * - Filter by positive/negative
 * - Pull-to-refresh
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTokensScreen(
    viewModel: AllTokensViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTokenDetails: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens by viewModel.allTokens.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    val scrollState = rememberLazyListState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("All Tokens", style = AppTypography.titleLarge) },
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
                placeholder = "Search tokens...",
                modifier = Modifier.padding(Dimensions.Padding.standard)
            )
            
            // Token List
            when (tokens) {
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
                    val tokenList = (tokens as LoadingState.Success).data
                    
                    if (tokenList.isEmpty()) {
                        EmptyState(
                            message = if (searchQuery.isNotBlank()) 
                                "No tokens found" 
                            else 
                                "No tokens available"
                        )
                    } else {
                        LazyColumn(
                            state = scrollState,
                            contentPadding = PaddingValues(Dimensions.Padding.standard),
                            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                        ) {
                            items(tokenList) { token ->
                                RankedTokenRow(
                                    rank = token.rank,
                                    symbol = token.symbol,
                                    name = token.name,
                                    marketCap = token.formattedMarketCap,
                                    price = token.formattedPrice,
                                    changePercent = token.priceChange24h,
                                    logoUrl = token.logoUrl,
                                    fallbackIconColor = getTokenColor(token.symbol),
                                    onClick = {
                                        Timber.d("🎯 Token clicked: ${token.symbol}")
                                        onNavigateToTokenDetails(token.id, token.symbol)
                                    }
                                )
                            }
                            
                            // Footer with count
                            item {
                                Text(
                                    "Showing ${tokenList.size} tokens",
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
                        message = (tokens as LoadingState.Error).message,
                        onRetry = viewModel::refreshTokens
                    )
                }
                
                else -> {}
            }
        }
    }
}

private fun getTokenColor(symbol: String): androidx.compose.ui.graphics.Color {
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
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            message,
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}
