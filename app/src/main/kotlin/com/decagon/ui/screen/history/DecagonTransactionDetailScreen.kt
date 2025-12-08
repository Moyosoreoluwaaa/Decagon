package com.decagon.ui.screen.history

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonTransaction
import com.decagon.domain.model.TransactionStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonTransactionDetailScreen(
    transactionId: String,
    onBackClick: () -> Unit,
    viewModel: DecagonTransactionDetailViewModel = koinViewModel()
) {
    val transaction by viewModel.getTransaction(transactionId).collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = transaction) {
            is DecagonLoadingState.Loading -> {
                LoadingView(Modifier.padding(padding))
            }
            is DecagonLoadingState.Success -> {
                TransactionDetailContent(
                    transaction = state.data,
                    onViewInExplorer = {
                        state.data.signature?.let { sig ->
                            val url = "https://solscan.io/tx/$sig"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier.padding(padding)
                )
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
private fun TransactionDetailContent(
    transaction: DecagonTransaction,
    onViewInExplorer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (transaction.status) {
                    TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer
                    TransactionStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer
                    TransactionStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = transaction.status.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = when (transaction.status) {
                        TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
                        TransactionStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                        TransactionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )

                Text(
                    text = "${transaction.amount} SOL",
                    style = MaterialTheme.typography.displaySmall,
                    color = when (transaction.status) {
                        TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
                        TransactionStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                        TransactionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )

                Text(
                    text = formatDateTime(transaction.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = when (transaction.status) {
                        TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.onPrimaryContainer
                        TransactionStatus.PENDING -> MaterialTheme.colorScheme.onSecondaryContainer
                        TransactionStatus.FAILED -> MaterialTheme.colorScheme.onErrorContainer
                    }
                )
            }
        }

        // Transaction Details
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleMedium
                )

                DetailRow(
                    label = "From",
                    value = transaction.from,
                    isAddress = true
                )

                Divider()

                DetailRow(
                    label = "To",
                    value = transaction.to,
                    isAddress = true
                )

                Divider()

                DetailRow(
                    label = "Amount",
                    value = "${transaction.amount} SOL"
                )

                Divider()

                DetailRow(
                    label = "Fee",
                    value = "${transaction.fee / 1_000_000_000.0} SOL"
                )

                Divider()

                DetailRow(
                    label = "Lamports",
                    value = "${transaction.lamports}"
                )

                transaction.signature?.let { sig ->
                    Divider()

                    DetailRow(
                        label = "Signature",
                        value = sig,
                        isAddress = true
                    )
                }
            }
        }

        // View in Explorer Button
        if (transaction.signature != null) {
            Button(
                onClick = onViewInExplorer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.OpenInNew, null)
                Spacer(Modifier.width(8.dp))
                Text("View on Solscan")
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isAddress: Boolean = false
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = if (isAddress) FontFamily.Monospace else FontFamily.Default,
            maxLines = if (isAddress) 2 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis
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

private fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.US).format(Date(timestamp))
}