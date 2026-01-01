package com.decagon.ui.screen.all

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.decagon.ui.components.FilterPanel
import com.decagon.ui.components.RankedTokenRow
import com.decagon.ui.components.SearchBarWithFilter
import com.octane.wallet.domain.models.Token
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.wallet.core.util.LoadingState
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllTokensScreen(
    viewModel: AllTokensViewModel = koinViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTokenDetails: (String, String) -> Unit
) {
    val tokens by viewModel.allTokens.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showFilters by viewModel.showFilters.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val showOnlyPositive by viewModel.showOnlyPositive.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val scrollState = rememberLazyListState()
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
                title = { Text("Tokens", style = AppTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding()) // Starts right under TopBar
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refreshTokens,
                modifier = Modifier.fillMaxSize()
            ) {
                if (tokens is LoadingState.Success) {
                    LazyColumn(
                        state = scrollState,
                        contentPadding = PaddingValues(
                            // This is the visible height of the floating search bar
                            top = searchBarVisibleHeight,
                            start = 16.dp, end = 16.dp, bottom = 16.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                    ) {
                        items((tokens as LoadingState.Success).data, key = { it.id }) { token ->
                            SwipeableTokenRow(
                                token = token,
                                rank = token.rank,
                                onClick = { onNavigateToTokenDetails(token.id, token.symbol) },
                                onFavorite = { /* ... */ },
                                onBuy = { /* ... */ }
                            )
                        }
                    }
                }
            }

            // Floating Search Bar & Filters
            // No Column-level padding here to avoid the "40dp gap"
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
                    onToggleFilters = viewModel::onToggleFilters,
                    showFilters = showFilters,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                AnimatedVisibility(
                    visible = showFilters,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    FilterPanel(
                        sortType = sortType,
                        onSortTypeChanged = viewModel::onSortTypeChanged,
                        showOnlyPositive = showOnlyPositive,
                        onTogglePositiveOnly = viewModel::onTogglePositiveOnly,
                        onResetFilters = viewModel::onResetFilters,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
private fun SwipeableTokenRow(
    token: Token,
    rank: Int,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onBuy: () -> Unit
) {
    val swipeableState = rememberSwipeableState(0)
    val sizePx = with(LocalDensity.current) { 160.dp.toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1)
    val haptic = LocalHapticFeedback.current

    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(Dimensions.Avatar.large),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFavorite()
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Outlined.StarBorder,
                    "Favorite",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBuy()
                },
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Rounded.ShoppingCart,
                    "Buy",
                    tint = AppColors.Solana,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        RankedTokenRow(
            rank = rank,
            symbol = token.symbol,
            name = token.name,
            marketCap = token.formattedMarketCap,
            price = "$${token.currentPrice}",
            changePercent = token.priceChange24h,
            logoUrl = token.logoUrl,
            fallbackIconColor = getTokenColor(token.symbol),
            onClick = onClick,
            modifier = Modifier
                .offset { IntOffset(swipeableState.offset.value.roundToInt(), 0) }
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) },
                    orientation = Orientation.Horizontal
                )
        )
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(message, color = AppColors.TextSecondary)
    }
}

private fun getTokenColor(symbol: String) = when (symbol.uppercase()) {
    "SOL" -> AppColors.Solana
    "BTC" -> AppColors.Bitcoin
    "ETH" -> AppColors.Ethereum
    else -> AppColors.TextSecondary
}