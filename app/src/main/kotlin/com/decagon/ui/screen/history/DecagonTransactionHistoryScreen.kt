package com.decagon.ui.screen.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.model.TransactionStatus
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonTransactionHistoryScreen(
    onBackClick: () -> Unit,
    onTransactionClick: (String) -> Unit,
    viewModel: DecagonTransactionHistoryViewModel = koinViewModel()
) {
    val groupedState by viewModel.groupedTransactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = groupedState) {
            is DecagonLoadingState.Loading -> {
                LoadingView(Modifier.padding(padding))
            }
            is DecagonLoadingState.Success -> {
                if (state.data.isEmpty()) {
                    EmptyView(Modifier.padding(padding))
                } else {
                    TransactionList(
                        groupedTransactions = state.data,
                        onTransactionClick = onTransactionClick,
                        modifier = Modifier.padding(padding)
                    )
                }
            }
            is DecagonLoadingState.Error -> {
                ErrorView(
                    message = state.message,
                    modifier = Modifier.padding(padding)
                )
            }
            is DecagonLoadingState.Idle -> {}
        }
    }
}

@Composable
private fun TransactionList(
    groupedTransactions: Map<String, List<DecagonTransaction>>,
    onTransactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedTransactions.forEach { (dateGroup, transactions) ->
            item {
                Text(
                    text = dateGroup,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(transactions, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) }
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: DecagonTransaction,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Icon + Direction
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Transaction icon
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when (transaction.status) {
                        TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                        TransactionStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                        TransactionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.CallMade, // Sent icon (can vary by direction)
                        contentDescription = null,
                        modifier = Modifier.padding(8.dp),
                        tint = when (transaction.status) {
                            TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
                            TransactionStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                            TransactionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }

                Column {
                    Text(
                        text = "Sent to",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = transaction.to.take(8) + "...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = formatTime(transaction.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Right: Amount + Status
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "- ${transaction.amount} SOL",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error
                )
                
                StatusChip(status = transaction.status)
            }
        }
    }
}

@Composable
private fun StatusChip(status: TransactionStatus) {
    Surface(
        shape = MaterialTheme.shapes.extraSmall,
        color = when (status) {
            TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
            TransactionStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
            TransactionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        }
    ) {
        Text(
            text = status.name,
            style = MaterialTheme.typography.labelSmall,
            color = when (status) {
                TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
                TransactionStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                TransactionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun LoadingView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Your transaction history will appear here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
            color = MaterialTheme.colorScheme.error
        )
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("hh:mm a", Locale.US).format(Date(timestamp))
}