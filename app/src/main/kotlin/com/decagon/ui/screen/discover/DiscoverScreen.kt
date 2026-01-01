package com.decagon.ui.screen.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.decagon.ui.navigation.UnifiedBottomNavBar
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.models.Token
import com.octane.wallet.presentation.components.PriceChangeBadge
import com.octane.wallet.presentation.components.SiteRow
import com.octane.wallet.presentation.components.shimmerEffect
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.viewmodel.DiscoverViewModel
import com.octane.wallet.presentation.viewmodel.SearchSuggestion
import com.octane.wallet.presentation.viewmodel.SortType
import com.wallet.core.util.LoadingState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val trendingTokens by viewModel.trendingTokens.collectAsState()
    val perps by viewModel.perps.collectAsState()
    val dapps by viewModel.dapps.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showScrollToTop by viewModel.showScrollToTop.collectAsState()

    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(scrollState) {
        snapshotFlow {
            scrollState.firstVisibleItemIndex to scrollState.firstVisibleItemScrollOffset
        }
            .distinctUntilChanged()
            .collect { (index, offset) ->
                viewModel.onScrollStateChanged(index, offset)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discover", style = AppTypography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = { UnifiedBottomNavBar(navController = navController) },
        floatingActionButton = {
            AnimatedVisibility(
                visible = showScrollToTop,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                SmallFloatingActionButton(
                    onClick = { scope.launch { scrollState.animateScrollToItem(0) }},
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    Icon(Icons.Rounded.ArrowUpward, "Scroll to top")
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refreshAll() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                state = scrollState,
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ==================== TOKENS (HORIZONTAL CARDS) ====================
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Tokens",
                                style = AppTypography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onNavigateToAllTokens) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        when (trendingTokens) {
                            is LoadingState.Success -> {
                                val tokens = (trendingTokens as LoadingState.Success).data.take(10)
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(tokens, key = { it.id }) { token ->
                                        TokenCard(
                                            token = token,
                                            onClick = {
                                                onNavigateToTokenDetails(
                                                    token.id,
                                                    token.symbol
                                                )
                                            }
                                        )
                                    }
                                }
                            }

                            is LoadingState.Loading -> {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(3) {
                                        Box(
                                            modifier = Modifier
                                                .size(160.dp, 120.dp)
                                                .shimmerEffect()
                                        )
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }

                // ==================== PERPS (HORIZONTAL CARDS) ====================
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Perps",
                                style = AppTypography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onNavigateToAllPerps) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        when (perps) {
                            is LoadingState.Success -> {
                                val perpList = (perps as LoadingState.Success).data.take(10)
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(perpList, key = { it.symbol }) { perp ->
                                        PerpCard(
                                            perp = perp,
                                            onClick = { onNavigateToPerpDetails(perp.symbol) }
                                        )
                                    }
                                }
                            }

                            is LoadingState.Loading -> {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(3) {
                                        Box(
                                            modifier = Modifier
                                                .size(160.dp, 140.dp)
                                                .shimmerEffect()
                                        )
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                }

                // ==================== DAPPS (VERTICAL LIST) ====================
                item {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Dapps",
                                style = AppTypography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = onNavigateToAllDApps) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                when (dapps) {
                    is LoadingState.Success -> {
                        val dappList = (dapps as LoadingState.Success).data.take(5)
                        items(dappList, key = { it.url }) { dapp ->
                            SiteRow(
                                name = dapp.name,
                                category = dapp.category.name.lowercase()
                                    .replaceFirstChar { it.uppercase() },
                                logoUrl = dapp.logoUrl,
                                onClick = { onNavigateToDAppDetails(dapp.url) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    is LoadingState.Loading -> {
                        items(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .padding(horizontal = 16.dp)
                                    .shimmerEffect()
                            )
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

// ==================== TOKEN CARD ====================
@Composable
private fun TokenCard(
    token: Token,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(160.dp, 120.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = token.logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                )
                Column {
                    Text(
                        token.symbol.uppercase(),
                        style = AppTypography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        token.name,
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column {
                Text(
                    token.formattedPrice,
                    style = AppTypography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PriceChangeBadge(changePercent = token.priceChange24h)
            }
        }
    }
}

// ==================== PERP CARD ====================
@Composable
private fun PerpCard(
    perp: Perp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.size(160.dp, 140.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box {
                    AsyncImage(
                        model = perp.logoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .offset(x = 20.dp, y = 20.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AllInclusive,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Text(
                    perp.symbol.uppercase(),
                    style = AppTypography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column {
                Text(
                    "$${perp.indexPrice}",
                    style = AppTypography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                PriceChangeBadge(changePercent = perp.priceChange24h)
                Text(
                    "Funding: ${perp.formattedFundingRate}",
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
