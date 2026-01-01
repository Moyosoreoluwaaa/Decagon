package com.decagon.ui.screen.token

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import com.decagon.ui.components.ChartActionGrid
import com.decagon.ui.components.EnhancedPriceChart
import com.decagon.ui.screen.perps.AssetInfoCard
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters
import com.wallet.core.util.LoadingState
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenDetailsScreen(
    tokenId: String,
    symbol: String,
    viewModel: TokenDetailViewModel = koinViewModel(),
    onBack: () -> Unit,
    onNavigateToSend: (String) -> Unit,
    onNavigateToReceive: (String) -> Unit,
    onNavigateToSwap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tokenDetail by viewModel.tokenDetail.collectAsState()
    val chartData by viewModel.chartData.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isInWatchlist by viewModel.isInWatchlist.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // Scroll animations
    val scrollProgress = (scrollState.value / 250f).coerceIn(0f, 1f)

    val token = (tokenDetail as? LoadingState.Success)?.data

    // Toast handling
    LaunchedEffect(toastMessage) {
        toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    LaunchedEffect(tokenId) {
        viewModel.loadToken(tokenId, symbol)
    }

    Scaffold(
        topBar = {
            AssetDetailTopBar(
                title = token?.name ?: symbol.uppercase(),
                subtitle = token?.let { UiFormatters.formatUsd(it.currentPrice) } ?: "",
                logoUrl = token?.logoUrl,
                scrollProgress = scrollProgress,
                isInWatchlist = isInWatchlist,
                onBack = onBack,
                onToggleWatchlist = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleWatchlist()
                },
                onSetAlert = { viewModel.setPriceAlert() },
                subtitleColor = if ((token?.priceChange24h
                        ?: 0.0) > 0
                ) AppColors.Success else AppColors.Error
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
            when (tokenDetail) {
                is LoadingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is LoadingState.Success -> {
                    val token = (tokenDetail as LoadingState.Success).data

                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(Dimensions.Padding.standard),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                    ) {
                        // ==================== ANIMATED HEADER ====================
                        AssetDetailHeader(
                            name = token.name,
                            symbol = token.symbol,
                            logoUrl = token.logoUrl,
                            currentPrice = UiFormatters.formatUsd(token.currentPrice),
                            priceChangePercent = token.priceChange24h,
                            scrollProgress = scrollProgress
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
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center)
                                        )
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
                                            color = AppColors.TextSecondary
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

                        // ==================== ACTION BUTTONS ====================
                        ChartActionGrid(
                            onReceive = { onNavigateToReceive(symbol) },
                            onCashBuy = { /* TODO: Navigate to on-ramp */ },
                            onShare = { /* TODO: Share */ },
                            onMore = { /* TODO: More options */ }
                        )

                        // ==================== INFO SECTION ====================
                        AssetInfoCard(title = "Token Info") {
                            InfoRow(
                                label = "Name",
                                value = token.name,
                                showDivider = true
                            )
                            InfoRow(
                                label = "Symbol",
                                value = token.symbol.uppercase(),
                                showDivider = true
                            )
                            InfoRow(
                                label = "Market Cap",
                                value = token.formattedMarketCap,
                                showDivider = true
                            )
                            InfoRow(
                                label = "24h Volume",
                                value = UiFormatters.formatCompactNumber(token.volume24h),
                                showDivider = token.mintAddress != null
                            )
                            if (token.mintAddress != null) {
                                InfoRow(
                                    label = "Mint Address",
                                    value = UiFormatters.formatAddress(token.mintAddress),
                                    showDivider = false
                                )
                            }
                        }
                    }
                }

                is LoadingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Error loading token",
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadToken(tokenId, symbol) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Solana
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