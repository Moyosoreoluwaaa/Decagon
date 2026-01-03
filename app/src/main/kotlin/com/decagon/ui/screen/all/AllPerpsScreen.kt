package com.decagon.ui.screen.all


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.decagon.ui.components.PerpRow
import com.decagon.ui.components.SearchBarWithFilter
import com.decagon.ui.theme.AppColors
import com.decagon.ui.theme.AppTypography
import com.decagon.ui.theme.Dimensions
import com.decagon.core.util.LoadingState
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllPerpsScreen(
    viewModel: AllPerpsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToPerpDetails: (String) -> Unit
) {
    val perps by viewModel.allPerps.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showFilters by viewModel.showFilters.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val density = LocalDensity.current
    // SearchBar standard height is roughly 56dp, adding minimal padding
    val searchBarVisibleHeight = 5.dp
    val searchBarHeightPx = with(density) { searchBarVisibleHeight.toPx() }

    var searchBarOffsetPx by remember { mutableFloatStateOf(0f) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                searchBarOffsetPx = (searchBarOffsetPx + delta).coerceIn(-searchBarHeightPx, 0f)
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Perpetuals", style = AppTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(top = padding.calculateTopPadding())) {
            PullToRefreshBox(isRefreshing = isRefreshing, onRefresh = viewModel::refreshPerps) {
                if (perps is LoadingState.Success) {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            top = searchBarVisibleHeight,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                    ) {
                        items((perps as LoadingState.Success).data) { perp ->
                            PerpRow(
                                symbol = perp.symbol,
                                name = perp.name,
                                price = "$${perp.indexPrice}",
                                changePercent = perp.priceChange24h,
                                volume24h = perp.formattedOpenInterest,
                                leverageMax = perp.leverage.replace("x", "").toIntOrNull()
                                    ?: 20,
                                logoUrl = perp.logoUrl,
                                fallbackIconColor = getTokenColor(
                                    perp.symbol.split("-").first()
                                ),
                                onClick = {
                                    Timber.d("🎯 Perp clicked: ${perp.symbol}")
                                    onNavigateToPerpDetails(perp.symbol)
                                }
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        translationY = searchBarOffsetPx
                        alpha = 1f + (searchBarOffsetPx / searchBarHeightPx)
                    }
            ) {
                SearchBarWithFilter(
                    searchQuery = searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onResetSearch = { viewModel.onSearchQueryChanged("") },
                    onToggleFilters = { },
                    showFilters = showFilters,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
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