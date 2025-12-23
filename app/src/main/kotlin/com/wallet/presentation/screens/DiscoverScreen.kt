package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.decagon.ui.navigation.UnifiedBottomNavBar
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.models.Token
import com.octane.wallet.presentation.components.*
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.viewmodel.DiscoverViewModel
import com.wallet.core.util.LoadingState
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = koinViewModel(),
    navController: NavController,
    onNavigateToTokenDetails: (String, String) -> Unit,
    onNavigateToPerpDetails: (String) -> Unit,
    onNavigateToDAppDetails: (String) -> Unit,
    onNavigateToAllTokens: () -> Unit,
    onNavigateToAllPerps: () -> Unit,
    onNavigateToAllDApps: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val trendingTokens by viewModel.trendingTokens.collectAsState()
    val perps by viewModel.perps.collectAsState()
    val dapps by viewModel.dapps.collectAsState()

    Scaffold(
        bottomBar = { UnifiedBottomNavBar(navController = navController) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                SearchInput(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = "Search tokens, perps, dApps..."
                )
            }

            // ========== TRENDING TOKENS SECTION ==========
            item {
                SectionHeader(
                    title = "Trending Tokens",
                    onViewAll = onNavigateToAllTokens
                )
            }

            when (trendingTokens) {
                is LoadingState.Loading -> {
                    items(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .shimmerEffect()
                        )
                    }
                }
                is LoadingState.Success -> {
                    val tokens = (trendingTokens as LoadingState.Success).data.take(5)
                    if (tokens.isEmpty()) {
                        item { EmptyState("No tokens available") }
                    } else {
                        items(tokens) { token ->
                            RankedTokenRow(
                                rank = tokens.indexOf(token) + 1,
                                symbol = token.symbol,
                                name = token.name,
                                marketCap = token.formattedMarketCap,
                                price = token.formattedPrice,
                                changePercent = token.priceChange24h,
                                logoUrl = token.logoUrl,
                                fallbackIconColor = getTokenColor(token.symbol),
                                onClick = { onNavigateToTokenDetails(token.id, token.symbol) }
                            )
                        }
                    }
                }
                is LoadingState.Error -> {
                    item {
                        ErrorScreen(
                            message = (trendingTokens as LoadingState.Error).message,
                            onRetry = {}
                        )
                    }
                }
                else -> {}
            }

            // Divider
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ========== PERPS SECTION ==========
            item {
                SectionHeader(
                    title = "Trending Perps",
                    onViewAll = onNavigateToAllPerps
                )
            }

            when (perps) {
                is LoadingState.Loading -> {
                    items(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .shimmerEffect()
                        )
                    }
                }
                is LoadingState.Success -> {
                    val perpList = (perps as LoadingState.Success).data.take(5)
                    if (perpList.isEmpty()) {
                        item { EmptyState("Perps coming soon") }
                    } else {
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
                                onClick = { onNavigateToPerpDetails(perp.symbol) }
                            )
                        }
                    }
                }
                is LoadingState.Error -> {
                    item {
                        ErrorScreen(
                            message = (perps as LoadingState.Error).message,
                            onRetry = {}
                        )
                    }
                }
                else -> {}
            }

            // Divider
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

            // ========== DAPPS SECTION ==========
            item {
                SectionHeader(
                    title = "Trending DApps",
                    onViewAll = onNavigateToAllDApps
                )
            }

            when (dapps) {
                is LoadingState.Loading -> {
                    items(3) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(70.dp)
                                .shimmerEffect()
                        )
                    }
                }
                is LoadingState.Success -> {
                    val dappList = (dapps as LoadingState.Success).data.take(5)
                    if (dappList.isEmpty()) {
                        item { EmptyState("No dApps available") }
                    } else {
                        items(dappList) { dapp ->
                            SiteRow(
                                rank = dappList.indexOf(dapp) + 1,
                                name = dapp.name,
                                category = dapp.category.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                logoUrl = dapp.logoUrl,
                                onClick = { onNavigateToDAppDetails(dapp.url) }
                            )
                        }
                    }
                }
                is LoadingState.Error -> {
                    item {
                        ErrorScreen(
                            message = (dapps as LoadingState.Error).message,
                            onRetry = {}
                        )
                    }
                }
                else -> {}
            }

            // Bottom spacer
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onViewAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = AppTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        TextButton(onClick = onViewAll) {
            Text(
                "View All",
                style = AppTypography.bodyMedium,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                modifier = Modifier.size(16.dp),
                tint = AppColors.TextPrimary
            )
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