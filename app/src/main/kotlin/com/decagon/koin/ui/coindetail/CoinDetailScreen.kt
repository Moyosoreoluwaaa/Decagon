package com.koin.ui.coindetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import android.widget.Toast
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.koin.data.coin.TimeRange
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    state: CoinDetailUiState,
    onEvent: (CoinDetailUiEvent) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coin = state.coin
    val selectedTimeRange = state.selectedTimeRange
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current

    // Handle toast messages
    LaunchedEffect(state.toastMessage) {
        state.toastMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            onEvent(CoinDetailUiEvent.ClearToast)
        }
    }

    // Calculate scroll progress for animations
    val scrollProgress = (scrollState.value / 500f).coerceIn(0f, 1f)
    val headerAlpha = 1f - scrollProgress
    val topBarContentAlpha = scrollProgress

    fun onTimeRangeSelected(timeRange: TimeRange) {
        onEvent(CoinDetailUiEvent.TimeRangeSelected(timeRange))
    }

    val priceHistory = state.historicalData.map { it.price }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.alpha(topBarContentAlpha)
                    ) {
                        // Animated coin image in top bar
                        AnimatedVisibility(
                            visible = scrollProgress > 0.3f,
                            enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(),
                            exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                        ) {
                            AsyncImage(
                                model = coin?.imageUrl,
                                contentDescription = "${coin?.name} logo",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(18.dp))
                        }

                        Column {
                            Text(
                                coin?.name ?: "Loading...",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            // Animated price in top bar
                            AnimatedVisibility(
                                visible = scrollProgress > 0.5f,
                                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                exit = slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                            ) {
                                Text(
                                    text = coin?.formattedPrice ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (coin?.isPositive24h == true) Color.Green else Color.Red
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Star button for watchlist
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEvent(CoinDetailUiEvent.ToggleWatchlist)
                        }
                    ) {
                        Icon(
                            imageVector = if (state.isInWatchlist) {
                                Icons.Filled.Star
                            } else {
                                Icons.Outlined.StarBorder
                            },
                            contentDescription = if (state.isInWatchlist) {
                                "Remove from watchlist"
                            } else {
                                "Add to watchlist"
                            },
                            tint = if (state.isInWatchlist) {
                                Color(0xFFFFD700) // Gold color for filled star
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                    // Price Alert button
                    IconButton(
                        onClick = {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            onEvent(CoinDetailUiEvent.SetPriceAlert)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = "Set Price Alert"
                        )
                    }
                })
        }
    )
    { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onEvent(CoinDetailUiEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding) // Apply padding to the PullToRefreshBox itself
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                coin == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Coin not found")
                    }
                }

                else -> {
                    Column(
                        modifier = modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState) // <--- Correctly applied here
                            .padding(8.dp) // Apply padding specific to the column content
                    ) {
                        // Header Section with fade animation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(headerAlpha)
                                .scale(1f - (scrollProgress * 0.1f)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                AsyncImage(
                                    model = coin.imageUrl,
                                    contentDescription = "${coin.name} logo",
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = coin.symbol.uppercase(),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = coin.formattedPrice,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = coin.formattedPriceChange,
                                    color = if (coin.isPositive24h) Color.Green else Color.Red,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))

                        // Enhanced Price Chart
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(MaterialTheme.colorScheme.background),
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                when {
                                    state.isLoadingHistoricalData -> {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator()
                                        }
                                    }

                                    priceHistory.isNotEmpty() -> {
                                        val isPositive = priceHistory.last() > priceHistory.first()
                                        EnhancedPriceChart(
                                            prices = priceHistory,
                                            modifier = Modifier.fillMaxSize(),
                                            lineColor = if (isPositive) Color.Green else Color.Red,
                                            gradientColors = listOf(
                                                if (isPositive)
                                                    Color.Green.copy(alpha = 0.3f)
                                                else
                                                    Color.Red.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    }

                                    !coin.sparklineData.isNullOrEmpty() -> {
                                        EnhancedPriceChart(
                                            prices = coin.sparklineData,
                                            modifier = Modifier.fillMaxSize(),
                                            lineColor = if (coin.isPositive24h) Color.Green else Color.Red,
                                            gradientColors = listOf(
                                                if (coin.isPositive24h)
                                                    Color.Green.copy(alpha = 0.2f)
                                                else
                                                    Color.Red.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    }

                                    else -> {
                                        Text(
                                            "No price data available",
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.align(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // Time Range Selector
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            items(TimeRange.entries) { timeRange ->
                                TimeRangeButton(
                                    text = timeRange.displayName,
                                    isSelected = timeRange == selectedTimeRange,
                                    onClick = { onTimeRangeSelected(timeRange) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // Market Stats
                        Text(
                            "Market Stats",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Stats Grid
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(MaterialTheme.shapes.medium)
                                .background(MaterialTheme.colorScheme.background)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatItem("Market Cap", coin.formattedMarketCap)
                            StatItem("24h Trading Volume", coin.formattedVolume)
                            StatItem(
                                "Circulating Supply",
                                "${coin.formattedSupply} ${coin.symbol.uppercase()}"
                            )
                            coin.supplyPercentage?.let { percentage ->
                                StatItem(
                                    "Supply Percentage",
                                    "${String.format(Locale.US, "%.2f", percentage)}%"
                                )
                            }
                            coin.high24h?.let { high ->
                                StatItem("24h High", formatCurrency(high))
                            }
                            coin.low24h?.let { low ->
                                StatItem("24h Low", formatCurrency(low))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        // About Section
                        Text(
                            "About ${coin.name}",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            "${coin.name} is a decentralized digital currency, without a central bank or single administrator, " +
                                    "that can be sent from user to user on the peer-to-peer bitcoin network without the need for intermediaries.",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}


@Composable
private fun TimeRangeButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        ),
        modifier = modifier
            .height(32.dp)
            .padding(horizontal = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1
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
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}
