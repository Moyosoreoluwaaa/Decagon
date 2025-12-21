package com.octane.wallet.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.decagon.ui.navigation.UnifiedBottomNavBar
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.models.Token
import com.octane.wallet.presentation.components.BottomNavBar
import com.octane.wallet.presentation.components.ErrorScreen
import com.octane.wallet.presentation.components.LearnCard
import com.octane.wallet.presentation.components.ListSectionHeader
import com.octane.wallet.presentation.components.ModeSelectorTabs
import com.octane.wallet.presentation.components.PerpRow
import com.octane.wallet.presentation.components.RankedTokenRow
import com.octane.wallet.presentation.components.SearchInput
import com.octane.wallet.presentation.components.ShimmerLoadingScreen
import com.octane.wallet.presentation.components.SiteRow
import com.octane.wallet.presentation.components.shimmerEffect
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.viewmodel.DiscoverMode
import com.octane.wallet.presentation.viewmodel.DiscoverViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscoverScreen(
    viewModel: DiscoverViewModel = koinViewModel(),
    navController: NavController,
    modifier: Modifier = Modifier,
    onNavigateToTokenDetails: (String, String) -> Unit,
    onNavigateToPerpDetails: (String) -> Unit,// 1. ADD THIS PARAMETER
    onNavigateToDAppDetails: (String) -> Unit,
    onTokenArrow: () -> Unit,
    onPerpArrow: () -> Unit,
    onDAppArrow: () -> Unit
) {
    Timber.d("🎨 DiscoverScreen composing...")

    // ==================== State ====================

    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val trendingTokens by viewModel.trendingTokens.collectAsState()
    val tokenSearchResults by viewModel.tokenSearchResults.collectAsState()

    val perps by viewModel.perps.collectAsState()
    val perpSearchResults by viewModel.perpSearchResults.collectAsState()

    val dapps by viewModel.dapps.collectAsState()
    val dappSearchResults by viewModel.dappSearchResults.collectAsState()

    // ✅ Pager state for swipeable tabs
    val pagerState = rememberPagerState(
        initialPage = when (selectedMode) {
            DiscoverMode.TOKENS -> 0
            DiscoverMode.PERPS -> 1
            DiscoverMode.LISTS -> 2
        },
        pageCount = { 3 }
    )

    // ✅ Separate scroll states for each tab
    val tokensScrollState = rememberLazyListState()
    val perpsScrollState = rememberLazyListState()
    val listsScrollState = rememberLazyListState()

    val scope = rememberCoroutineScope()

    // ✅ FIXED: Get current scroll state based on PAGER page, not selectedMode
    val currentScrollState = remember(pagerState.currentPage) {
        when (pagerState.currentPage) {
            0 -> tokensScrollState
            1 -> perpsScrollState
            2 -> listsScrollState
            else -> tokensScrollState
        }
    }

    // ✅ Calculate search bar visibility based on CURRENT tab's scroll
    val searchBarAlpha by remember {
        derivedStateOf {
            val firstVisibleIndex = currentScrollState.firstVisibleItemIndex
            val firstVisibleOffset = currentScrollState.firstVisibleItemScrollOffset

            when {
                firstVisibleIndex == 0 && firstVisibleOffset < 100 -> 1f // Fully visible
                firstVisibleIndex == 0 -> (1f - (firstVisibleOffset / 300f)).coerceIn(
                    0f,
                    1f
                ) // Fading
                else -> 0f // Hidden
            }
        }
    }

    // ✅ Animate alpha changes smoothly
    val animatedAlpha by animateFloatAsState(
        targetValue = searchBarAlpha,
        animationSpec = tween(durationMillis = 300), // Increased from 200ms for smoother feel
        label = "searchBarAlpha"
    )

    // Sync pager state with selected mode
    LaunchedEffect(pagerState.currentPage) {
        val newMode = when (pagerState.currentPage) {
            0 -> DiscoverMode.TOKENS
            1 -> DiscoverMode.PERPS
            2 -> DiscoverMode.LISTS
            else -> DiscoverMode.TOKENS
        }
        if (newMode != selectedMode) {
            Timber.d("🎨 Pager changed to page ${pagerState.currentPage}, updating mode to $newMode")
            viewModel.onModeSelected(newMode)
        }
    }

    // ✅ Check if ALL tabs are loading for full-screen shimmer
    val isFullScreenLoading = remember(trendingTokens, perps, dapps) {
        trendingTokens is LoadingState.Loading &&
                perps is LoadingState.Loading &&
                dapps is LoadingState.Loading
    }

    // ==================== UI ====================

    Scaffold(
        bottomBar = { UnifiedBottomNavBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ✅ Show full-screen shimmer if ALL data is loading
            if (isFullScreenLoading) {
                ShimmerLoadingScreen()
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // ==================== COLLAPSING HEADER ====================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Dimensions.Padding.standard)
                            .padding(top = Dimensions.Spacing.large)
                    ) {
                        // ✅ Search Input - Fades out on scroll
                        SearchInput(
                            query = searchQuery,
                            onQueryChange = { newQuery ->
                                Timber.d("🎨 Search input changed: '$newQuery'")
                                viewModel.onSearchQueryChanged(newQuery)
                            },
                            placeholder = "Sites, tokens, URL",
                        )
                    }

                    // ✅ Mode Tabs - Always visible
                    ModeSelectorTabs(
                        modes = listOf("Tokens", "Perps", "Lists"),
                        selectedMode = when (selectedMode) {
                            DiscoverMode.TOKENS -> "Tokens"
                            DiscoverMode.PERPS -> "Perps"
                            DiscoverMode.LISTS -> "Lists"
                        },
                        onModeSelected = { mode ->
                            Timber.d("🎨 Tab clicked: $mode")
                            val newMode = when (mode) {
                                "Tokens" -> DiscoverMode.TOKENS
                                "Perps" -> DiscoverMode.PERPS
                                "Lists" -> DiscoverMode.LISTS
                                else -> DiscoverMode.TOKENS
                            }
                            viewModel.onModeSelected(newMode)

                            scope.launch {
                                val targetPage = when (newMode) {
                                    DiscoverMode.TOKENS -> 0
                                    DiscoverMode.PERPS -> 1
                                    DiscoverMode.LISTS -> 2
                                }
                                pagerState.animateScrollToPage(targetPage)
                            }
                        },
                    )

                    // ==================== SWIPEABLE CONTENT ====================
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                    ) { page ->
                        when (page) {
                            0 -> {
                                LazyColumn(
                                    state = tokensScrollState,
                                    contentPadding = PaddingValues(
                                        horizontal = Dimensions.Padding.standard,
                                        vertical = Dimensions.Spacing.small
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                                ) {
                                    renderTokensTab(
                                        searchQuery = searchQuery,
                                        trendingTokens = trendingTokens,
                                        searchResults = tokenSearchResults,
                                        onTokenClick = { token ->
                                            // 2. UNCOMMENT AND FIX THIS
                                            viewModel.onTokenClicked(token) // Keep for logging if needed
                                            onNavigateToTokenDetails(token.id, token.symbol)
                                        },
                                        onTokenArrow = onTokenArrow

                                    )
                                }
                            }

                            1 -> {
                                LazyColumn(
                                    state = perpsScrollState,
                                    contentPadding = PaddingValues(
                                        horizontal = Dimensions.Padding.standard,
                                        vertical = Dimensions.Spacing.small
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                                ) {
                                    renderPerpsTab(
                                        searchQuery = searchQuery,
                                        perps = perps,
                                        searchResults = perpSearchResults,
                                        onPerpClick = { perp ->
                                            // 3. ADD NAVIGATION CALL HERE
                                            viewModel.onPerpClicked(perp) // Keep for logging if needed
                                            onNavigateToPerpDetails(perp.symbol)
                                        },
                                        onPerpArrow = onPerpArrow

                                    )
                                }
                            }

                            2 -> {
                                LazyColumn(
                                    state = listsScrollState,
                                    contentPadding = PaddingValues(
                                        horizontal = Dimensions.Padding.standard,
                                        vertical = Dimensions.Spacing.small
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                                ) {
                                    renderListsTab(
                                        searchQuery = searchQuery,
                                        dapps = dapps,
                                        searchResults = dappSearchResults,
                                        onDAppClick = { dapp ->
                                            Timber.d("🎨 DApp row clicked: ${dapp.name}")
                                            viewModel.onDAppClicked(dapp)
                                            onNavigateToDAppDetails(dapp.url)
                                        },
                                        onDAppArrow = onDAppArrow
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        Timber.d("🎨 DiscoverScreen entered")
        onDispose {
            Timber.d("🎨 DiscoverScreen disposed")
        }
    }
}

// ==================== TOKENS TAB ====================

private fun LazyListScope.renderTokensTab(
    searchQuery: String,
    trendingTokens: LoadingState<List<Token>>,
    searchResults: LoadingState<List<Token>>,
    onTokenClick: (Token) -> Unit,
    onTokenArrow: () -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else trendingTokens

    item {
        ListSectionHeader(
            searchQuery = searchQuery,
            sectionTitle = "Trending DApps",
            onActionClick = onTokenArrow, // Pass the new click handler
        )
    }

    when (displayState) {
        is LoadingState.Loading -> {
            // Individual tab loading (not full screen)
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
                items(tokens.take(20)) { token ->
                    RankedTokenRow(
                        rank = tokens.indexOf(token) + 1,
                        symbol = token.symbol,
                        name = token.name,
                        marketCap = token.formattedMarketCap,
                        price = token.formattedPrice,
                        changePercent = token.priceChange24h,
                        logoUrl = token.logoUrl,
                        fallbackIconColor = getTokenColor(token.symbol),
                        onClick = { onTokenClick(token) }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            item {
                ErrorScreen(
                    message = displayState.message,
                    onRetry = { }
                )
            }
        }

        else -> {}
    }
}

// ==================== PERPS TAB ====================

private fun LazyListScope.renderPerpsTab(
    searchQuery: String,
    perps: LoadingState<List<Perp>>,
    searchResults: LoadingState<List<Perp>>,
    onPerpClick: (Perp) -> Unit,
    onPerpArrow: () -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else perps

    item {
        ListSectionHeader(
            searchQuery = searchQuery,
            sectionTitle = "Trending Perps",
            onActionClick = onPerpArrow, // Pass the new click handler
            // The existing code placed the IconButton below the Text,
            // which this composable now replicates.
        )
    }

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
                items(perpList.take(20)) { perp ->
                    PerpRow(
                        symbol = perp.symbol,
                        name = perp.name,
                        price = "$${perp.indexPrice}",
                        changePercent = perp.priceChange24h,
                        volume24h = perp.formattedOpenInterest,
                        leverageMax = perp.leverage.replace("x", "").toIntOrNull() ?: 20,
                        logoUrl = perp.logoUrl,
                        fallbackIconColor = getTokenColor(perp.symbol.split("-").first()),
                        onClick = { onPerpClick(perp) }
                    )
                }

                if (perpList.size > 20) {
                    item {
                        Text(
                            "Showing 20 of ${perpList.size} perps",
                            style = AppTypography.bodyMedium,
                            color = AppColors.TextSecondary,
                            modifier = Modifier.padding(vertical = Dimensions.Spacing.medium)
                        )
                    }
                }
            }
        }

        is LoadingState.Error -> {
            item {
                ErrorScreen(message = displayState.message, onRetry = { })
            }
        }

        else -> {}
    }
}

// ==================== LISTS TAB ====================

private fun LazyListScope.renderListsTab(
    searchQuery: String,
    dapps: LoadingState<List<DApp>>,
    searchResults: LoadingState<List<DApp>>,
    onDAppClick: (DApp) -> Unit,
    onDAppArrow: () -> Unit
) {
    val displayState = if (searchQuery.isNotBlank()) searchResults else dapps

    item {
        ListSectionHeader(
            searchQuery = searchQuery,
            sectionTitle = "Trending Perps",
            onActionClick = onDAppArrow, // Pass the new click handler
            // The existing code placed the IconButton below the Text,
            // which this composable now replicates.
        )
    }

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
                items(dappList.take(20)) { dapp ->
                    SiteRow(
                        rank = dappList.indexOf(dapp) + 1,
                        name = dapp.name,
                        category = dapp.category.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        logoUrl = dapp.logoUrl,
                        onClick = { onDAppClick(dapp) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
                    Text(
                        "Learn More >",
                        style = AppTypography.titleLarge,
                        color = AppColors.TextPrimary
                    )
                }

                item {
                    LearnCard(
                        title = "Getting Started with DeFi",
                        subtitle = "Learn the basics of decentralized finance",
                        iconColor = AppColors.Solana,
                        onClick = { }
                    )
                }
            }
        }

        is LoadingState.Error -> {
            item {
                ErrorScreen(message = displayState.message, onRetry = { })
            }
        }

        else -> {}
    }
}

// ==================== HELPERS ====================

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