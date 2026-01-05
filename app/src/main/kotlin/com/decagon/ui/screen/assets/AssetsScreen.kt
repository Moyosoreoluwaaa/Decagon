package com.decagon.ui.screen.assets// :ui:screen:assets:AssetsScreen.kt

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.decagon.R
import com.decagon.core.util.formatCompact
import com.decagon.core.util.formatCurrency
import com.decagon.core.util.formatPercentage
import com.decagon.domain.model.TokenBalance
import com.decagon.ui.navigation.UnifiedBottomNavBar
import com.decagon.ui.navigation.UnifiedRoute
import com.decagon.ui.theme.AppTypography
import com.decagon.util.ItemShape
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetsScreen(
    navController: NavController,
    viewModel: AssetsViewModel = koinViewModel()
) {
    val wallet by viewModel.activeWallet.collectAsState()
    val balances by viewModel.tokenBalances.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val totalValue by viewModel.totalPortfolioValue.collectAsState()
    val change24h by viewModel.portfolioChange24h.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assets", style = AppTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            if (isRefreshing) Icons.Rounded.Refresh else Icons.Rounded.Sync,
                            contentDescription = "Refresh"
                        )
                    }
                }
            )
        },
        bottomBar = { UnifiedBottomNavBar(navController) }
    ) { padding ->
        if (balances.isEmpty() && !isRefreshing) {
            // Empty state
            EmptyAssetsScreen(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Portfolio Summary Card
                item {
                    PortfolioSummaryCard(
                        totalValue = totalValue,
                        change24h = change24h,
                        isRefreshing = isRefreshing
                    )
                }
                
                // Section Header
                item {
                    Text(
                        text = "Your Tokens (${balances.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                // Token List
                items(
                    items = balances,
                    key = { it.mint }
                ) { balance ->
                    TokenBalanceFullItem(
                        balance = balance,
                        onClick = {
                            navController.navigate(
                                UnifiedRoute.TokenDetails(
                                    tokenId = balance.mint,
                                    symbol = balance.symbol
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioSummaryCard(
    totalValue: Double,
    change24h: Double,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = ItemShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Total Portfolio Value",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "$${totalValue.formatCurrency(2)}",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (change24h >= 0) {
                            Icons.AutoMirrored.Rounded.TrendingUp
                        } else {
                            Icons.AutoMirrored.Rounded.TrendingDown
                        },
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (change24h >= 0) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFF44336)
                        }
                    )
                    
                    Text(
                        text = "${if (change24h >= 0) "+" else ""}${change24h.formatPercentage(2)} (24h)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (change24h >= 0) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenBalanceFullItem(
    balance: TokenBalance,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = ItemShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Token Logo
            AsyncImage(
                model = balance.logoUrl,
                contentDescription = balance.symbol,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape),
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = balance.symbol,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = balance.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${balance.uiAmount.formatCompact(4)} ${balance.symbol}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${balance.valueUsd.formatCurrency(2)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                balance.change24h?.let { change ->
                    Text(
                        text = "${if (change >= 0) "+" else ""}${change.formatPercentage(2)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (change >= 0) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFF44336)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAssetsScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "No tokens yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Start by receiving tokens to your wallet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}