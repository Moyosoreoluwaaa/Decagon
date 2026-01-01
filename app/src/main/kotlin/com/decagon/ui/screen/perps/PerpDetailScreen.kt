package com.decagon.ui.screen.perps

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.decagon.ui.components.AssetDetailHeader
import com.decagon.ui.components.AssetDetailTopBar
import com.decagon.ui.components.EnhancedPriceChart
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.wallet.core.util.LoadingState
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
    // Scroll animations
    val scrollProgress = (scrollState.value / 250f).coerceIn(0f, 1f)

    val perp = (perpDetail as? LoadingState.Success)?.data

    // Toast handling
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(perpSymbol) {
        viewModel.loadPerp(perpSymbol)
    }


    Scaffold(
        topBar = {
            // ✅ Using Reusable Top Bar
            AssetDetailTopBar(
                title = perp?.symbol?.uppercase() ?: perpSymbol.uppercase(),
                subtitle = perp?.let { "$${it.indexPrice}" } ?: "",
                logoUrl = perp?.logoUrl,
                scrollProgress = scrollProgress,
                isInWatchlist = isInWatchlist,
                onBack = onBack,
                onToggleWatchlist = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleWatchlist()
                },
                onSetAlert = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.setPriceAlert()
                },
                subtitleColor = if ((perp?.priceChange24h ?: 0.0) > 0)
                    AppColors.Success else AppColors.Error
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
                is LoadingState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is LoadingState.Success -> {
                    val currentPerp = (perpDetail as LoadingState.Success).data

                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(Dimensions.Padding.standard),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                    ) {
                        // ==================== ANIMATED HEADER ====================
                        AssetDetailHeader(
                            name = currentPerp.name,
                            symbol = currentPerp.symbol,
                            logoUrl = currentPerp.logoUrl,
                            currentPrice = "$${currentPerp.indexPrice}",
                            priceChangePercent = currentPerp.priceChange24h,
                            scrollProgress = scrollProgress,
                            modifier = Modifier.padding(horizontal = Dimensions.Padding.standard)
                        )

                        // ==================== ENHANCED CHART ====================
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            shape = RoundedCornerShape(Dimensions.CornerRadius.standard),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                when (chartData) {
                                    is LoadingState.Loading -> {
                                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                                    }

                                    is LoadingState.Success -> {
                                        val prices = (chartData as LoadingState.Success).data
                                        if (prices.isNotEmpty()) {
                                            val isPositive = prices.last() > prices.first()
                                            EnhancedPriceChart(
                                                prices = prices,
                                                modifier = Modifier.fillMaxSize(),
                                                lineColor = if (isPositive)
                                                    AppColors.Success else AppColors.Error,
                                            )
                                        }
                                    }

                                    is LoadingState.Error -> {
                                        Text(
                                            "Chart unavailable",
                                            modifier = Modifier.align(Alignment.Center),
                                        )
                                    }

                                    else -> {}
                                }
                            }
                        }

                        // ==================== TIMEFRAME SELECTOR ====================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("1H", "1D", "1W", "1M", "YTD", "ALL").forEach { timeframe ->
                                FilterChip(
                                    selected = timeframe == selectedTimeframe,
                                    onClick = { viewModel.onTimeframeSelected(timeframe) },
                                    label = { Text(timeframe, style = AppTypography.labelMedium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                        selectedLabelColor = AppColors.Bitcoin,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    ),
                                    border = BorderStroke(0.dp, color = Color.Transparent)
                                )
                            }
                        }

                        // ==================== LEVERAGE SELECTOR ====================
                        Text(
                            "Leverage",
                            style = AppTypography.titleMedium,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2, 5, 10, 20).forEach { leverage ->
                                FilterChip(
                                    selected = leverage == selectedLeverage,
                                    onClick = { viewModel.onLeverageSelected(leverage) },
                                    label = { Text("${leverage}x") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        selectedLabelColor = Color.White,
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                    )
                                )
                            }
                        }

                        // ==================== TRADE BUTTONS ====================
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                        ) {
                            Button(
                                onClick = { onNavigateToTrade(perpSymbol) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Success
                                )
                            ) {
                                Text("Long", style = AppTypography.labelLarge)
                            }

                            Button(
                                onClick = { onNavigateToTrade(perpSymbol) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Error
                                )
                            ) {
                                Text("Short", style = AppTypography.labelLarge)
                            }
                        }

                        // ==================== PERP INFO ====================
                        AssetInfoCard(title = "Market Info") {
                            InfoRow(
                                label = "Mark Price",
                                value = "$${currentPerp.markPrice}",
                                showDivider = true
                            )
                            InfoRow(
                                label = "Index Price",
                                value = "$${currentPerp.indexPrice}",
                                showDivider = true
                            )
                            InfoRow(
                                label = "Funding Rate",
                                value = currentPerp.formattedFundingRate,
                                showDivider = true
                            )
                            InfoRow(
                                label = "Open Interest",
                                value = currentPerp.formattedOpenInterest,
                                showDivider = true
                            )
                            InfoRow(
                                label = "24h Volume",
                                value = "$${currentPerp.volume24h}",
                                showDivider = true
                            )
                            InfoRow(
                                label = "Exchange",
                                value = currentPerp.exchange,
                                showDivider = false
                            )
                        }
                    }
                }

                is LoadingState.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Error loading perp",
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadPerp(perpSymbol) },
                                colors = ButtonDefaults.buttonColors(
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }
}

// ==================== REDESIGNED INFO ROW ====================
@Composable
private fun InfoRow(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Dimensions.Padding.large),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = AppTypography.bodyMedium,
            )
            Text(
                text = value,
                style = AppTypography.bodyMedium,
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}

@Composable
fun AssetInfoCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Text(
            text = title,
            style = AppTypography.titleSmall,
            modifier = Modifier.padding(bottom = Dimensions.Spacing.small)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimensions.CornerRadius.standard),
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.Padding.standard),
                content = content
            )
        }
    }
}