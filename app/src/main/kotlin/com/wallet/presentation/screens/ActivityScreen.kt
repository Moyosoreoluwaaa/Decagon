package com.octane.wallet.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Transaction
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.domain.models.TransactionType
import com.octane.wallet.presentation.components.BottomNavBar
import com.octane.wallet.presentation.components.StatusBadge
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters
import com.octane.wallet.presentation.viewmodel.ActivityViewModel
import org.koin.androidx.compose.koinViewModel

/**
 * ✅ Separated Activity screen showing transaction history only.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    viewModel: ActivityViewModel = koinViewModel(),
    navController: NavController,
    onBack: () -> Unit,
    onNavigateToDetails: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredTransactions by viewModel.filteredTransactions.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberLazyListState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Activity", style = AppTypography.headlineSmall)
                        if (pendingCount > 0) {
                            Text(
                                "$pendingCount pending",
                                style = AppTypography.bodySmall,
                                color = AppColors.TextSecondary
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            "Back",
                            tint = AppColors.TextPrimary
                        )
                    }
                },
                actions = {
                    // Filter button
                    IconButton(onClick = { /* Show filter sheet */ }) {
                        Icon(
                            Icons.Rounded.FilterList,
                            "Filter",
                            tint = AppColors.TextPrimary
                        )
                    }
                    // Export button
                    IconButton(onClick = { viewModel.exportTransactions() }) {
                        Icon(
                            Icons.Rounded.Download,
                            "Export",
                            tint = AppColors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        },
        bottomBar = {
            BottomNavBar(
                navController = navController,
                onBackToHome = onBack
            )
        }
    ) { innerPadding ->
        when (val state = filteredTransactions) {
            is LoadingState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Success)
                }
            }

            is LoadingState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyActivityScreen()
                } else {
                    LazyColumn(
                        state = scrollState,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(Dimensions.Padding.standard),
                        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                    ) {
                        items(
                            items = state.data,
                            key = { it.txHash }
                        ) { transaction ->
                            TransactionRow(
                                transaction = transaction,
                                onClick = {
                                    viewModel.showTransactionDetails(transaction)
                                    onNavigateToDetails(transaction.txHash)
                                },
                                formatTransactionType = viewModel::formatTransactionType,
                                getTransactionIcon = viewModel::getTransactionIcon,
                                getStatusColor = viewModel::getStatusColor
                            )
                        }

                        // Load more indicator
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Dimensions.Padding.standard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = AppColors.Success
                                    )
                                }
                            }
                        }
                    }
                }
            }

            is LoadingState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(Dimensions.Padding.extraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = AppColors.Error,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Spacing.standard))
                    Text(
                        "Failed to load transactions",
                        style = AppTypography.titleMedium,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        state.message,
                        style = AppTypography.bodyMedium,
                        color = AppColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(Dimensions.Spacing.large))
                    Button(onClick = { /* Retry */ }) {
                        Text("Retry")
                    }
                }
            }

            else -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

/**
 * Individual transaction row component
 */
@Composable
private fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    formatTransactionType: (TransactionType) -> String,
    getTransactionIcon: (TransactionType) -> String,
    getStatusColor: (TransactionStatus) -> androidx.compose.ui.graphics.Color
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = AppColors.Surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.Padding.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Transaction Icon
                Box(
                    modifier = Modifier
                        .size(Dimensions.Avatar.medium)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(AppColors.SurfaceHighlight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        getTransactionIcon(transaction.type),
                        style = AppTypography.bodyLarge
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        formatTransactionType(transaction.type),
                        style = AppTypography.titleSmall,
                        color = AppColors.TextPrimary
                    )
                    Text(
                        UiFormatters.formatRelativeTime(transaction.timestamp),
                        style = AppTypography.bodySmall,
                        color = AppColors.TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${transaction.amount} ${transaction.tokenSymbol}",
                    style = AppTypography.titleSmall,
                    color = AppColors.TextPrimary
                )
                StatusBadge(status = transaction.status)
            }
        }
    }
}