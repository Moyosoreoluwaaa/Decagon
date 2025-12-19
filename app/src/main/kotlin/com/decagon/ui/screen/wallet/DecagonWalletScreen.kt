package com.decagon.ui.screen.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decagon.core.network.NetworkEnvironment
import com.decagon.core.network.NetworkManager
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonWallet
import com.decagon.domain.model.PortfolioHistoryPoint
import com.decagon.ui.components.DecagonQuickActionsBar
import com.decagon.ui.components.FloatingTopBar
import com.decagon.ui.components.NetworkSelectorModal
import com.decagon.ui.components.PortfolioPerformanceChart
import com.decagon.ui.components.TimeRange
import com.decagon.ui.components.TimeRangeSelector
import com.decagon.ui.components.WalletSwitcherModal
import com.decagon.ui.components.formatCurrency
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonWalletScreen(
    viewModel: DecagonWalletViewModel = koinViewModel(),
    networkManager: NetworkManager = koinInject(),
    onNavigateToOnboarding: () -> Unit = {},
    onNavigateToSettings: (String) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToBuy: () -> Unit = {},
    onNavigateToSwap: () -> Unit = {},
) {
    val walletState by viewModel.walletState.collectAsState()
    val allWallets by viewModel.allWallets.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val fiatPrice by viewModel.fiatPrice.collectAsState()
    val currentNetwork by networkManager.currentNetwork.collectAsState()

    val scope = rememberCoroutineScope()

    // UI State
    var showWalletSwitcher by remember { mutableStateOf(false) }
    var showNetworkSelector by remember { mutableStateOf(false) }
    var showSendSheet by remember { mutableStateOf(false) }
    var showReceiveSheet by remember { mutableStateOf(false) }
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    var selectedTimeRange by remember { mutableStateOf(TimeRange.ONE_WEEK) }

    // Dismiss snackbar after 2s
    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            delay(2000)
            showCopiedSnackbar = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background and main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A0F),
                            Color(0xFF12121A),
                            Color(0xFF1A1A24)
                        )
                    )
                )
        ) {
            when (val state = walletState) {
                is DecagonLoadingState.Loading -> {
                    LoadingView()
                }

                is DecagonLoadingState.Success -> {
                    ModernWalletContent(
                        wallet = state.data,
                        currentNetwork = currentNetwork,
                        selectedCurrency = selectedCurrency,
                        fiatPrice = fiatPrice,
                        selectedTimeRange = selectedTimeRange,
                        onProfileClick = { showWalletSwitcher = true },
                        onNetworkClick = { showNetworkSelector = true },
                        onNotificationClick = onNavigateToHistory,
                        onAddressCopy = {
                            showCopiedSnackbar = true
                        },
                        onCurrencyClick = { /* TODO: Show currency selector */ },
                        onSendClick = { showSendSheet = true },
                        onReceiveClick = { showReceiveSheet = true },
                        onBuyClick = onNavigateToBuy,
                        onSwapClick = onNavigateToSwap,
                        onTimeRangeChange = { selectedTimeRange = it }
                    )
                }

                is DecagonLoadingState.Error -> {
                    ErrorView(message = state.message)
                }

                is DecagonLoadingState.Idle -> {}
            }

            // Snackbar for copy confirmation
            if (showCopiedSnackbar) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                ) {
                    Text("Address copied to clipboard")
                }
            }
        }

        // Modals render on top
        if (showWalletSwitcher) {
            WalletSwitcherModal(
                wallets = allWallets,
                activeWalletId = (walletState as? DecagonLoadingState.Success)?.data?.id ?: "",
                onWalletSelect = { walletId ->
                    viewModel.switchWallet(walletId)
                },
                onAddWallet = onNavigateToOnboarding,
                onSettings = {
                    val wallet = (walletState as? DecagonLoadingState.Success)?.data
                    wallet?.let { onNavigateToSettings(it.id) }
                },
                onLogOut = { /* TODO */ },
                onDismiss = { showWalletSwitcher = false }
            )
        }

        if (showNetworkSelector) {
            NetworkSelectorModal(
                currentNetwork = currentNetwork,
                onNetworkSelect = { network ->
                    scope.launch {
                        networkManager.switchNetwork(network)
                        Timber.d("Network switched to: $network")
                    }
                },
                onDismiss = { showNetworkSelector = false }
            )
        }

        if (showSendSheet) {
            com.decagon.ui.components.DecagonSendSheet(
                onDismiss = { showSendSheet = false }
            )
        }

        if (showReceiveSheet && walletState is DecagonLoadingState.Success) {
            com.decagon.ui.components.DecagonReceiveSheet(
                wallet = (walletState as DecagonLoadingState.Success).data,
                onDismiss = { showReceiveSheet = false }
            )
        }
    }
}

@Composable
private fun ModernWalletContent(
    wallet: DecagonWallet,
    currentNetwork: NetworkEnvironment,
    selectedCurrency: String,
    fiatPrice: Double,
    selectedTimeRange: TimeRange,
    onProfileClick: () -> Unit,
    onNetworkClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAddressCopy: () -> Unit,
    onCurrencyClick: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    onBuyClick: () -> Unit,
    onSwapClick: () -> Unit,
    onTimeRangeChange: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val fiatValue = wallet.balance * fiatPrice

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Floating Top Bar
        FloatingTopBar(
            wallet = wallet,
            currentNetwork = currentNetwork,
            onProfileClick = onProfileClick,
            onNetworkClick = onNetworkClick,
            onNotificationClick = onNotificationClick,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Balance Hero Section
        BalanceHeroSection(
            wallet = wallet,
            fiatValue = fiatValue,
            fiatPrice = fiatPrice,
            selectedCurrency = selectedCurrency,
            onAddressCopy = {
                clipboardManager.setText(AnnotatedString(wallet.address))
                onAddressCopy()
            },
            onCurrencyClick = onCurrencyClick
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Portfolio Performance Chart
        PortfolioPerformanceChart(
            historyPoints = generateMockPortfolioHistory(fiatValue, selectedTimeRange),
            selectedTimeRange = selectedTimeRange,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Time Range Selector
        TimeRangeSelector(
            selected = selectedTimeRange,
            onSelect = onTimeRangeChange
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Quick Actions Bar (BELOW CHART)
        DecagonQuickActionsBar(
            wallet = wallet,
            onSendClick = onSendClick,
            onReceiveClick = onReceiveClick,
            onBuyClick = onBuyClick,
            onSwapClick = onSwapClick
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun BalanceHeroSection(
    wallet: DecagonWallet,
    fiatValue: Double,
    fiatPrice: Double,
    selectedCurrency: String,
    onAddressCopy: () -> Unit,
    onCurrencyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Truncated Address with Copy
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable(onClick = onAddressCopy)
        ) {
            Text(
                text = wallet.truncatedAddress,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color(0xFF7E7E8F)
                )
            )
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ContentCopy,
                contentDescription = null,
                tint = Color(0xFF7E7E8F),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dramatic Balance Typography
        Text(
            text = "%.4f ${wallet.activeChain?.chainType?.symbol ?: "SOL"}".format(wallet.balance),
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 56.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                letterSpacing = (-1).sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Fiat Value with Currency Selector
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onCurrencyClick)
        ) {
            Text(
                text = formatCurrency(fiatValue, selectedCurrency),
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFB4B4C6)
            )
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = Color(0xFFB4B4C6),
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Change Indicator (mock)
        Text(
            text = androidx.compose.ui.text.buildAnnotatedString {
                withStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        color = Color(0xFF14F195)
                    )
                ) {
                    append("+$72.30")
                }
                append(" ")
                withStyle(
                    style = androidx.compose.ui.text.SpanStyle(color = Color(0xFFB4B4C6))
                ) {
                    append("(5.24%) Today")
                }
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF9945FF)
        )
    }
}

@Composable
private fun ErrorView(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFFF6B6B)
        )
    }
}

private fun generateMockPortfolioHistory(
    currentValue: Double,
    timeRange: TimeRange
): List<PortfolioHistoryPoint> {
    val dataPoints = when (timeRange) {
        TimeRange.ONE_DAY -> 24
        TimeRange.ONE_WEEK -> 7
        TimeRange.ONE_MONTH -> 30
        TimeRange.ONE_YEAR -> 365
        TimeRange.ALL -> 365
    }

    return (0 until dataPoints).map { index ->
        val progress = index.toDouble() / (dataPoints - 1)
        val baseValue = currentValue * (0.8 + progress * 0.2)
        val noise = kotlin.math.sin(progress * 20) * (currentValue * 0.05) *
                kotlin.random.Random.nextDouble(-1.0, 1.0)

        PortfolioHistoryPoint(
            timestamp = System.currentTimeMillis() - ((dataPoints - index) * 3600000L),
            totalValueUsd = if (index == dataPoints - 1) currentValue else (baseValue + noise).coerceAtLeast(currentValue * 0.5)
        )
    }
}