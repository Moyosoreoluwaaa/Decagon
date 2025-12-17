package com.octane.wallet.presentation.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.wear.compose.material.ExperimentalWearMaterialApi
import androidx.wear.compose.material.FractionalThreshold
import androidx.wear.compose.material.rememberSwipeableState
import androidx.wear.compose.material.swipeable
import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.models.Token
import com.octane.wallet.presentation.components.BottomNavBar
import com.octane.wallet.presentation.components.DiscoverFilterPanel
import com.octane.wallet.presentation.components.DiscoverToolbar
import com.octane.wallet.presentation.components.ErrorScreen
import com.octane.wallet.presentation.components.ModeSelectorTabs
import com.octane.wallet.presentation.components.PerpRow
import com.octane.wallet.presentation.components.RankedTokenRow
import com.octane.wallet.presentation.components.SiteRow
import com.octane.wallet.presentation.components.shimmerEffect
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.viewmodel.DiscoverMode
import com.octane.wallet.presentation.viewmodel.DiscoverViewModel
import com.octane.wallet.presentation.viewmodel.SearchSuggestion
import com.octane.wallet.presentation.viewmodel.SortType
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = koinViewModel(),
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateToTokenDetails: (String, String) -> Unit,
    onNavigateToPerpDetails: (String) -> Unit,
    onNavigateToDAppDetails: (String) -> Unit
) {
    Timber.d("🎨 DiscoverScreen composing...")

    // State
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showFilters by viewModel.showFilters.collectAsState()
    val sortType by viewModel.sortType.collectAsState()
    val showOnlyPositive by viewModel.showOnlyPositive.collectAsState()
    val searchSuggestions by viewModel.searchSuggestions.collectAsState()

    val trendingTokens by viewModel.trendingTokens.collectAsState()
    val tokenSearchResults by viewModel.tokenSearchResults.collectAsState()
    val perps by viewModel.perps.collectAsState()
    val perpSearchResults by viewModel.perpSearchResults.collectAsState()
    val dapps by viewModel.dapps.collectAsState()
    val dappSearchResults by viewModel.dappSearchResults.collectAsState()

    val scrollState = when (selectedMode) {
        DiscoverMode.TOKENS -> rememberLazyListState()
        DiscoverMode.PERPS -> rememberLazyListState()
        DiscoverMode.LISTS -> rememberLazyListState()
    }

    val scope = rememberCoroutineScope()

    // Scroll tracking
    var isScrollingDown by remember { mutableStateOf(false) }
    var showToolbar by remember { mutableStateOf(true) }
    var showScrollToTop by remember { mutableStateOf(false) }
    var lastScrollIndex by remember { mutableIntStateOf(0) }
    var lastScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                val scrollingDown = when {
                    index > lastScrollIndex -> true
                    index < lastScrollIndex -> false
                    else -> offset > lastScrollOffset
                }
                isScrollingDown = scrollingDown
                showToolbar = (index == 0 && offset == 0) || !scrollingDown
                showScrollToTop = index > 0 || offset > 100

                lastScrollIndex = index
                lastScrollOffset = offset
            }
    }

    // Animations
    val toolbarAlpha by animateFloatAsState(
        targetValue = if (showToolbar) 1f else 0f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "toolbar_alpha"
    )

    val toolbarHeight by animateDpAsState(
        targetValue = if (showToolbar) 80.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "toolbar_height"
    )

    val fabScale by animateFloatAsState(
        targetValue = if (showScrollToTop) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fab_scale"
    )

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    scope.launch {
                        scrollState.animateScrollToItem(0)
                    }
                },
                containerColor = AppColors.Solana,
                contentColor = Color.White,
                modifier = Modifier
                    .scale(fabScale)
                    .alpha(fabScale)
            ) {
                Icon(Icons.Filled.ArrowUpward, "Scroll to top")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Collapsing toolbar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(toolbarHeight)
                        .alpha(toolbarAlpha)
                ) {
                    if (showToolbar) {
                        DiscoverToolbar(
                            searchQuery = searchQuery,
                            onQueryChange = { viewModel.onSearchQueryChanged(it) },
                            onResetSearch = { viewModel.onSearchQueryChanged("") },
                            onToggleFilters = { viewModel.onToggleFilters() },
                            showFilters = showFilters,
                            onResetFilters = { viewModel.onResetFilters() },
                            showResetButton = searchQuery.isNotEmpty() ||
                                    sortType != SortType.RANK ||
                                    showOnlyPositive,
                            searchSuggestions = searchSuggestions,
                            onSuggestionClick = { suggestion ->
                                when (suggestion) {
                                    is SearchSuggestion.TokenSuggestion ->
                                        viewModel.onSearchQueryChanged(suggestion.token.name)

                                    is SearchSuggestion.PerpSuggestion ->
                                        viewModel.onSearchQueryChanged(suggestion.perp.name)

                                    is SearchSuggestion.DAppSuggestion ->
                                        viewModel.onSearchQueryChanged(suggestion.dapp.name)
                                }
                            }
                        )
                    }
                }

                // Mode tabs
                ModeSelectorTabs(
                    modes = listOf("Tokens", "Perps", "Lists"),
                    selectedMode = when (selectedMode) {
                        DiscoverMode.TOKENS -> "Tokens"
                        DiscoverMode.PERPS -> "Perps"
                        DiscoverMode.LISTS -> "Lists"
                    },
                    onModeSelected = { mode ->
                        val newMode = when (mode) {
                            "Tokens" -> DiscoverMode.TOKENS
                            "Perps" -> DiscoverMode.PERPS
                            else -> DiscoverMode.LISTS
                        }
                        viewModel.onModeSelected(newMode)
                    }
                )

                // Filter panel
                androidx.compose.animation.AnimatedVisibility(
                    visible = showFilters,
                    enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                    exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                ) {
                    DiscoverFilterPanel(
                        sortType = sortType,
                        onSortTypeChanged = { viewModel.onSortTypeChanged(it) },
                        showOnlyPositive = showOnlyPositive,
                        onTogglePositiveOnly = { viewModel.onTogglePositiveOnly() }
                    )
                }

                // Content
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { viewModel.refreshAll() },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    LazyColumn(
                        state = scrollState,
                        contentPadding = PaddingValues(
                            horizontal = Dimensions.Padding.standard,
                            vertical = Dimensions.Spacing.small
                        ),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                    ) {
                        when (selectedMode) {
                            DiscoverMode.TOKENS -> {
                                renderTokensTab(
                                    searchQuery = searchQuery,
                                    trendingTokens = trendingTokens,
                                    searchResults = tokenSearchResults,
                                    onTokenClick = { token ->
                                        viewModel.onTokenClicked(token)
                                        onNavigateToTokenDetails(token.id, token.symbol)
                                    },
                                    onTokenFavorite = { token ->
                                        viewModel.onTokenFavorited(token)
                                    },
                                    onTokenBuy = { token ->
                                        viewModel.onTokenBuyClicked(token)
                                    }
                                )
                            }

                            DiscoverMode.PERPS -> {
                                renderPerpsTab(
                                    searchQuery = searchQuery,
                                    perps = perps,
                                    searchResults = perpSearchResults,
                                    onPerpClick = { perp ->
                                        viewModel.onPerpClicked(perp)
                                        onNavigateToPerpDetails(perp.symbol)
                                    },
                                    onPerpFavorite = { perp ->
                                        { }
                                    },
                                    onPerpTrade = { perp ->
                                        { }
                                    }
                                )
                            }

                            DiscoverMode.LISTS -> {
                                renderListsTab(
                                    searchQuery = searchQuery,
                                    dapps = dapps,
                                    searchResults = dappSearchResults,
                                    onDAppClick = { dapp ->
                                        viewModel.onDAppClicked(dapp)
                                        onNavigateToDAppDetails(dapp.url)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        Timber.d("🎨 DiscoverScreen entered")
        onDispose { Timber.d("🎨 DiscoverScreen disposed") }
    }
}

// ==================== TOKENS TAB ====================

@OptIn(ExperimentalWearMaterialApi::class)
private fun androidx.compose.foundation.lazy.LazyListScope.renderTokensTab(
    searchQuery: String,
    trendingTokens: LoadingState<List<Token>>,
    searchResults: LoadingState<List<Token>>,
    onTokenClick: (Token) -> Unit,
    onTokenFavorite: (Token) -> Unit,
    onTokenBuy: (Token) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else trendingTokens

    when (displayState) {
        is LoadingState.Loading -> {
            items(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .shimmerEffect()
                )
            }
        }

        is LoadingState.Success -> {
            val tokens = displayState.data

            if (tokens.isEmpty()) {
                item {
                    EmptyState(
                        message = if (searchQuery.isNotBlank())
                            "No tokens found"
                        else
                            "No trending tokens available"
                    )
                }
            } else {
                items(tokens, key = { it.id }) { token ->
                    SwipeableTokenRow(
                        token = token,
                        rank = tokens.indexOf(token) + 1,
                        onClick = { onTokenClick(token) },
                        onFavorite = { onTokenFavorite(token) },
                        onBuy = { onTokenBuy(token) }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            item {
                ErrorScreen(message = displayState.message, onRetry = {})
            }
        }

        else -> {}
    }
}

// ==================== PERPS TAB ====================

@OptIn(ExperimentalWearMaterialApi::class)
private fun androidx.compose.foundation.lazy.LazyListScope.renderPerpsTab(
    searchQuery: String,
    perps: LoadingState<List<Perp>>,
    searchResults: LoadingState<List<Perp>>,
    onPerpClick: (Perp) -> Unit,
    onPerpFavorite: (Perp) -> Unit,
    onPerpTrade: (Perp) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else perps

    when (displayState) {
        is LoadingState.Loading -> {
            items(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .shimmerEffect()
                )
            }
        }

        is LoadingState.Success -> {
            val perpList = displayState.data

            if (perpList.isEmpty()) {
                item {
                    EmptyState(message = "Perpetual futures coming soon")
                }
            } else {
                items(perpList, key = { it.symbol }) { perp ->
                    SwipeablePerpRow(
                        perp = perp,
                        onClick = { onPerpClick(perp) },
                        onFavorite = { onPerpFavorite(perp) },
                        onTrade = { onPerpTrade(perp) }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            item {
                ErrorScreen(message = displayState.message, onRetry = {})
            }
        }

        else -> {}
    }
}

// ==================== LISTS TAB ====================

private fun androidx.compose.foundation.lazy.LazyListScope.renderListsTab(
    searchQuery: String,
    dapps: LoadingState<List<DApp>>,
    searchResults: LoadingState<List<DApp>>,
    onDAppClick: (DApp) -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else dapps

    when (displayState) {
        is LoadingState.Loading -> {
            items(5) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                        .shimmerEffect()
                )
            }
        }

        is LoadingState.Success -> {
            val dappList = displayState.data

            if (dappList.isEmpty()) {
                item {
                    EmptyState(message = "No dApps found")
                }
            } else {
                items(dappList.take(20), key = { it.url }) { dapp ->
                    SiteRow(
                        rank = dappList.indexOf(dapp) + 1,
                        name = dapp.name,
                        category = dapp.category.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        logoUrl = dapp.logoUrl,
                        onClick = { onDAppClick(dapp) }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            item {
                ErrorScreen(message = displayState.message, onRetry = {})
            }
        }

        else -> {}
    }
}

// ==================== SWIPEABLE TOKEN ROW ====================

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
private fun SwipeableTokenRow(
    token: Token,
    rank: Int,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val sizePx = with(LocalDensity.current) { 160.dp.toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1)

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onFavorite,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = onBuy,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    contentDescription = "Buy",
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
            price = token.formattedPrice,
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

// ==================== SWIPEABLE PERP ROW ====================

@OptIn(ExperimentalWearMaterialApi::class)
@Composable
private fun SwipeablePerpRow(
    perp: Perp,
    onClick: () -> Unit,
    onFavorite: () -> Unit,
    onTrade: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val sizePx = with(LocalDensity.current) { 160.dp.toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1)

    Box(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onFavorite,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = onTrade,
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = "Trade",
                    tint = AppColors.Solana,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        PerpRow(
            symbol = perp.symbol,
            name = perp.name,
            price = "$${perp.indexPrice}",
            changePercent = perp.priceChange24h,
            volume24h = perp.formattedOpenInterest,
            leverageMax = perp.leverage.replace("x", "").toIntOrNull() ?: 20,
            logoUrl = perp.logoUrl,
            fallbackIconColor = getTokenColor(perp.symbol.split("-").first()),
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

// ==================== HELPERS ====================

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
            style = MaterialTheme.typography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}