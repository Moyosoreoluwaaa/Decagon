package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.octane.wallet.presentation.components.BottomNavBar
import com.octane.wallet.presentation.components.InfoRow
import com.octane.wallet.presentation.components.SwapCard
import com.octane.wallet.presentation.components.WideActionButton
import com.wallet.presentation.navigation.AppRoute
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters
import com.octane.wallet.presentation.viewmodel.SwapEvent
import com.octane.wallet.presentation.viewmodel.SwapViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * Swap screen for token exchanges.
 * Connected to SwapViewModel.
 */
@Composable
fun SwapScreen(
    viewModel: SwapViewModel = koinViewModel(),
    navController: NavController, // ✅ Pass navController
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val swapState by viewModel.swapState.collectAsState()
    val portfolioState by viewModel.portfolioState.collectAsState()

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SwapEvent.Success -> onSuccess(event.txHash)
                is SwapEvent.Error -> {
                    // Show error toast
                }

                is SwapEvent.HighPriceImpactWarning -> {
                    // Show warning dialog
                }
            }
        }
    }

    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) } // ✅ Fixed
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            SwapHeader(
                onBack = onBack,
                onSettingsClick = { navController.navigate(AppRoute.Settings) }
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.Padding.standard),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
            ) {
                // Swap Cards
                SwapCard(
                    payingToken = swapState.fromToken,
                    payingAmount = swapState.fromAmount,
                    receivingToken = swapState.toToken,
                    receivingAmount = swapState.toAmount,
                    onFlip = viewModel::reverseTokens
                )

                // Rate Info
                if (swapState.rate != null) {
                    InfoRow(
                        label = "Rate",
                        value = UiFormatters.formatExchangeRate(
                            fromAmount = 1.0,
                            fromSymbol = swapState.fromToken,
                            toAmount = swapState.rate ?: 0.0,
                            toSymbol = swapState.toToken
                        ),
                        showDivider = false
                    )
                }

                // Price Impact Warning
                if (swapState.priceImpact != null && swapState.priceImpact!! > 1.0) {
                    val (color, impact) = UiFormatters.formatPriceImpact(swapState.priceImpact!!)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Price Impact:",
                            style = AppTypography.bodyMedium,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            impact,
                            style = AppTypography.bodyMedium,
                            color = color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.Spacing.medium))

                // Swap Button
                WideActionButton(
                    text = if (swapState.isFetchingQuote) "Fetching Quote..." else "Review Swap",
                    isPrimary = true,
                    enabled = swapState.isValid && !swapState.isFetchingQuote,
                    onClick = { /* Navigate to confirmation */ }
                )
            }
        }
    }
}

@Composable
private fun SwapHeader(
    onBack: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimensions.Padding.standard),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = AppColors.TextPrimary
            )
        }

        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = "Settings",
                tint = AppColors.TextPrimary
            )
        }
    }
}