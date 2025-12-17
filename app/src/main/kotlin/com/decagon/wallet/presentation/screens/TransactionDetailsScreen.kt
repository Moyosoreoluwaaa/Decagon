package com.octane.wallet.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.wallet.domain.models.TransactionStatus
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.UiFormatters
import com.octane.wallet.presentation.viewmodel.BaseTransactionViewModel
import com.octane.wallet.presentation.viewmodel.TransactionDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    txHash: String,
    viewModel: TransactionDetailsViewModel = koinViewModel(),
    baseViewModel: BaseTransactionViewModel = koinInject(),
    onBack: () -> Unit
) {
    val transaction by viewModel.transaction.collectAsState()
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { uriHandler.openUri(viewModel.getExplorerUrl()) }
                    ) {
                        Icon(Icons.Rounded.OpenInNew, "View on Explorer")
                    }
                }
            )
        }
    ) { padding ->
        transaction?.let { tx ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(Dimensions.Padding.large),
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large)
            ) {
                // Status Card
                StatusCard(
                    status = tx.status,
                    getStatusColor = baseViewModel::getStatusColor
                )

                // Amount Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(Dimensions.Padding.large),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            baseViewModel.getTransactionIcon(tx.type),
                            style = AppTypography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(Dimensions.Spacing.small))
                        Text(
                            "${tx.amount} ${tx.tokenSymbol}",
                            style = AppTypography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            baseViewModel.formatTransactionType(tx.type),
                            style = AppTypography.bodyLarge,
                            color = AppColors.TextSecondary
                        )
                    }
                }

                // Transaction Details
                DetailSection("Transaction Details") {
                    DetailRow("Type", baseViewModel.formatTransactionType(tx.type))
                    DetailRow("Status", tx.status.name)
                    DetailRow(
                        "Time",
                        UiFormatters.formatRelativeTime(tx.timestamp)
                    )
                    tx.confirmationCount?.let {
                        DetailRow("Confirmations", it.toString())
                    }
                }

                // Addresses
                DetailSection("Addresses") {
                    DetailRow(
                        "From",
                        tx.fromAddress,
                        isCopyable = true
                    )
                    tx.toAddress?.let {
                        DetailRow("To", it, isCopyable = true)
                    }
                }

                // Fees & Network
                DetailSection("Network & Fees") {
                    DetailRow("Network", "Solana")
                    DetailRow("Fee", "${tx.fee} SOL")
                    DetailRow(
                        "Transaction Hash",
                        viewModel.formatTxHash(),
                        isCopyable = true,
                        fullValue = tx.txHash
                    )
                }

                // Error message if failed
                if (tx.status == TransactionStatus.FAILED && tx.errorMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = AppColors.Error.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(Dimensions.Padding.medium),
                            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                        ) {
                            Icon(
                                Icons.Rounded.Error,
                                contentDescription = null,
                                tint = AppColors.Error
                            )
                            Column {
                                Text(
                                    "Transaction Failed",
                                    style = AppTypography.titleSmall,
                                    color = AppColors.Error,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    tx.errorMessage ?: "Unknown error",
                                    style = AppTypography.bodySmall,
                                    color = AppColors.TextSecondary
                                )
                            }
                        }
                    }
                }

                // View on Explorer Button
                OutlinedButton(
                    onClick = { uriHandler.openUri(viewModel.getExplorerUrl()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.OpenInNew, null)
                    Spacer(modifier = Modifier.width(Dimensions.Spacing.small))
                    Text("View on Solscan")
                }
            }
        } ?: LoadingScreen()
    }
}

@Composable
private fun StatusCard(
    status: TransactionStatus,
    getStatusColor: (TransactionStatus) -> Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = getStatusColor(status).copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Dimensions.Padding.large),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
        ) {
            Icon(
                when (status) {
                    TransactionStatus.CONFIRMED -> Icons.Rounded.CheckCircle
                    TransactionStatus.PENDING -> Icons.Rounded.Schedule
                    TransactionStatus.FAILED -> Icons.Rounded.Error
                },
                contentDescription = null,
                tint = getStatusColor(status),
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    status.name,
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getStatusColor(status)
                )
                Text(
                    when (status) {
                        TransactionStatus.CONFIRMED -> "Transaction finalized on Solana"
                        TransactionStatus.PENDING -> "Waiting for confirmation..."
                        TransactionStatus.FAILED -> "Transaction was not successful"
                    },
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
        Text(
            title,
            style = AppTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = AppColors.Surface
            )
        ) {
            Column(
                modifier = Modifier.padding(Dimensions.Padding.large),
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium),
                content = content
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isCopyable: Boolean = false,
    fullValue: String = value
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                value,
                style = AppTypography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = AppColors.TextPrimary
            )
            if (isCopyable) {
                Icon(
                    Icons.Rounded.ContentCopy,
                    contentDescription = "Copy",
                    tint = AppColors.TextSecondary,
                    modifier = Modifier
                        .size(Dimensions.IconSize.small)
                        .clickable {
                            val clipboard = context.getSystemService(
                                android.content.ClipboardManager::class.java
                            )
                            val clip = android.content.ClipData.newPlainText(label, fullValue)
                            clipboard.setPrimaryClip(clip)
                        }
                )
            }
        }
    }
}