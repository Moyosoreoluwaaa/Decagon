package com.octane.wallet.presentation.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.decagon.wallet.presentation.components.EnhancedPriceChart
import com.decagon.wallet.presentation.viewmodel.PerpDetailViewModel
import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.Dimensions
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerpDetailScreen(
    perpSymbol: String,
    viewModel: PerpDetailViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToTrade: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val perpDetail by viewModel.perpDetail.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val selectedLeverage by viewModel.selectedLeverage.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(perpSymbol) {
        viewModel.loadPerp(perpSymbol)
    }

    // Scroll tracking
    var showTopBarContent by remember { mutableStateOf(false) }
    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .collect { offset ->
                showTopBarContent = offset > 200
            }
    }

    val topBarContentAlpha by animateFloatAsState(
        targetValue = if (showTopBarContent) 1f else 0f,
        animationSpec = tween(200),
        label = "topbar_alpha"
    )

    val perp = (perpDetail as? LoadingState.Success)?.data

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(topBarContentAlpha)
                    ) {
                        AnimatedVisibility(
                            visible = showTopBarContent,
                            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = perp?.logoUrl,
                                    contentDescription = "${perp?.name} logo",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        perp?.symbol?.uppercase() ?: perpSymbol.uppercase(),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = perp?.let { "$${it.indexPrice}" } ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (perp?.priceChange24h ?: 0.0 > 0) AppColors.Success else AppColors.Error
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.toggleWatchlist()
                        }
                    ) {
                        Icon(
                            imageVector = if (isInWatchlist) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isInWatchlist) "Remove from watchlist" else "Add to watchlist",
                            tint = if (isInWatchlist) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.setPriceAlert()
                        }
                    ) {
                        Icon(Icons.Outlined.NotificationsNone, contentDescription = "Set Price Alert")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
            )
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (perpDetail) {
                is LoadingState.Success -> {
                    val perp = (perpDetail as LoadingState.Success).data

                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(Dimensions.Padding.standard)
                    ) {
                        // Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                AsyncImage(
                                    model = perp.logoUrl,
                                    contentDescription = "${perp.name} logo",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = perp.symbol.uppercase(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$${perp.indexPrice}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${if (perp.priceChange24h > 0) "+" else ""}${String.format("%.2f", perp.priceChange24h)}%",
                                    color = if (perp.priceChange24h > 0) AppColors.Success else AppColors.Error,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(280.dp)
                        ) {
                            when (chartData) {
                                is LoadingState.Loading -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }

                                is LoadingState.Success -> {
                                    val prices = (chartData as LoadingState.Success).data
                                    if (prices.isNotEmpty()) {
                                        val isPositive = prices.last() > prices.first()
                                        EnhancedPriceChart(
                                            prices = prices,
                                            modifier = Modifier.fillMaxSize(),
                                            lineColor = if (isPositive) AppColors.Success else AppColors.Error,
                                            gradientColors = listOf(
                                                if (isPositive) AppColors.Success.copy(alpha = 0.3f) else AppColors.Error.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    }
                                }

                                is LoadingState.Error -> {
                                    Text(
                                        "Chart unavailable",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                else -> {}
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Timeframe selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("1D", "1W", "1M", "1Y").forEach { timeframe ->
                                TimeframeButton(
                                    text = timeframe,
                                    isSelected = timeframe == selectedTimeframe,
                                    onClick = { viewModel.onTimeframeSelected(timeframe) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Leverage selector
                        Text(
                            "Leverage",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                        ) {
                            listOf(2, 5, 10, 20).forEach { leverage ->
                                FilterChip(
                                    selected = leverage == selectedLeverage,
                                    onClick = { viewModel.onLeverageSelected(leverage) },
                                    label = {
                                        Text(
                                            "${leverage}x",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AppColors.Solana,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Trade buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
                        ) {
                            Button(
                                onClick = { onNavigateToTrade(perpSymbol) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success)
                            ) {
                                Text("Long", style = MaterialTheme.typography.labelLarge)
                            }
                            Button(
                                onClick = { onNavigateToTrade(perpSymbol) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)
                            ) {
                                Text("Short", style = MaterialTheme.typography.labelLarge)
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Perp info
                        Text(
                            "Perp Info",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(AppColors.Surface)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatItem("Mark Price", "$${perp.markPrice}")
                            StatItem("Index Price", "$${perp.indexPrice}")
                            StatItem("Funding Rate", perp.formattedFundingRate)
                            StatItem("Open Interest", perp.formattedOpenInterest)
                            StatItem("24h Volume", "$${perp.volume24h}")
                            StatItem("Exchange", perp.exchange)
                            StatItem("Max Leverage", perp.leverage)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // About
                        Text(
                            "About ${perp.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            "${perp.name} perpetual futures allow traders to speculate on price movements with leverage up to ${perp.leverage}.",
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 24.sp,
                            color = AppColors.TextSecondary
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }

                is LoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error loading perp")
                            Button(onClick = { viewModel.loadPerp(perpSymbol) }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is LoadingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                else -> {}
            }
        }
    }
}

@Composable
private fun TimeframeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) AppColors.Solana else AppColors.SurfaceHighlight,
            contentColor = if (isSelected) Color.White else AppColors.TextSecondary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.height(40.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = AppColors.TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = AppColors.TextPrimary
        )
    }
}