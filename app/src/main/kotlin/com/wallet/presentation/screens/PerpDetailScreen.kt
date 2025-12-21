package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.presentation.components.DetailHeader
import com.octane.wallet.presentation.components.ErrorScreen
import com.octane.wallet.presentation.components.InfoRow
import com.octane.wallet.presentation.components.MetallicCard
import com.octane.wallet.presentation.components.ShimmerLoadingScreen
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters
import com.octane.wallet.presentation.viewmodel.PerpDetailViewModel
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
    // Extract Perp data for TopBar usage
    val perp: Perp? = when (val state = perpDetail) {
        is LoadingState.Success -> state.data
        else -> null
    }

    LaunchedEffect(perpSymbol) {
        viewModel.loadPerp(perpSymbol)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Logo (AsyncImage)
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .padding(end = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // ✅ Perp Logo
                            if (perpDetail is LoadingState.Success) {
                                val perp = (perpDetail as LoadingState.Success).data
                                AsyncImage(
                                    model = perp.logoUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // ✅ FIXED: Display Symbol and "Perp" in a Column <-- THIS IS THE TARGET BLOCK
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                // Primary Symbol (e.g., WIP-USDC)
                                perpSymbol.uppercase(),
                                style = AppTypography.headlineSmall,
                                color = AppColors.TextPrimary
                            )
                            Text(
                                // Sub-text "Perp" indicator
                                "Perp",
                                style = AppTypography.labelMedium,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
            )
        }
    ) { padding ->
        val pad = padding
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                horizontal = Dimensions.Padding.standard,
                vertical = Dimensions.Spacing.large
            ),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
        ) {

            when (val state = perpDetail) {
                is LoadingState.Success -> {
                    val perp = state.data

                    // Price Header with Chart
                    item {
                        DetailHeader(
                            price = UiFormatters.formatUsd(perp.indexPrice), // <--- CHANGE
                            changeAmount = "$${(perp.indexPrice * (perp.priceChange24h / 100.0))}",
                            changePercent = perp.priceChange24h,
                            isPositive = perp.priceChange24h >= 0,
                            selectedTimeframe = selectedTimeframe,
                            onTimeframeSelected = viewModel::onTimeframeSelected,
                            chartState = chartData
                        )
                    }

                    // Leverage Selector
                    item {
                        Text(
                            "Leverage",
                            style = AppTypography.titleMedium,
                            color = AppColors.TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                        ) {
                            listOf(2, 5, 10, 20).forEach { leverage ->
                                FilterChip(
                                    selected = leverage == selectedLeverage,
                                    onClick = { viewModel.onLeverageSelected(leverage) },
                                    label = { Text("${leverage}x") }
                                )
                            }
                        }
                    }

                    // Trade Buttons
                    item {
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
                                Text("Long")
                            }

                            Button(
                                onClick = { onNavigateToTrade(perpSymbol) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppColors.Error
                                )
                            ) {
                                Text("Short")
                            }
                        }
                    }

                    // Perp Info
                    item {
                        Text(
                            "Info",
                            style = AppTypography.titleLarge,
                            color = AppColors.TextPrimary
                        )

                        MetallicCard(modifier = Modifier.fillMaxWidth()) {
                            Column {
                                InfoRow("Mark Price", "$${perp.markPrice}")
                                InfoRow("Index Price", "$${perp.indexPrice}")
                                InfoRow("Funding Rate", perp.formattedFundingRate)
                                InfoRow("Open Interest", perp.formattedOpenInterest)
                                InfoRow("24h Volume", "$${perp.volume24h}")
                                InfoRow("Exchange", perp.exchange)
                            }
                        }
                    }
                }

                is LoadingState.Loading -> {
                    item { ShimmerLoadingScreen() }
                }

                is LoadingState.Error -> {
                    item {
                        ErrorScreen(
                            message = state.message,
                            onRetry = { viewModel.loadPerp(perpSymbol) }
                        )
                    }
                }

                else -> {}
            }
        }
    }
}