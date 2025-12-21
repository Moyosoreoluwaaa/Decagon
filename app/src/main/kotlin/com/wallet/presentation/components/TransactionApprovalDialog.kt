package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wallet.core.blockchain.TransactionRequest
import com.octane.wallet.domain.models.TransactionType
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography

@Composable
fun TransactionApprovalDialog(
    dappUrl: String,
    request: TransactionRequest,
    walletName: String,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isProcessing by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onReject() },
        icon = {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = AppColors.Warning
            )
        },
        title = {
            Text(
                text = when (request.type) {
                    TransactionType.SINGLE -> "Approve Transaction"
                    TransactionType.MULTIPLE -> "Approve Multiple Transactions"
                    TransactionType.MESSAGE -> "Sign Message"
                    TransactionType.SEND -> TODO()
                    TransactionType.RECEIVE -> TODO()
                    TransactionType.SWAP -> TODO()
                    TransactionType.STAKE -> TODO()
                    TransactionType.UNSTAKE -> TODO()
                    TransactionType.CLAIM_REWARDS -> TODO()
                    TransactionType.APPROVE -> TODO()
                    TransactionType.REVOKE -> TODO()
                    TransactionType.NFT_MINT -> TODO()
                    TransactionType.NFT_TRANSFER -> TODO()
                },
                style = AppTypography.titleLarge
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // DApp Info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Surface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Requesting DApp",
                            style = AppTypography.labelSmall,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = extractDomain(dappUrl),
                            style = AppTypography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Wallet Info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Surface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Wallet",
                            style = AppTypography.labelSmall,
                            color = AppColors.TextSecondary
                        )
                        Text(
                            text = walletName,
                            style = AppTypography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Transaction Details
                when (request.type) {
                    TransactionType.MESSAGE -> {
                        Text(
                            text = "The DApp is requesting you to sign a message. This won't cost any SOL.",
                            style = AppTypography.bodySmall,
                            color = AppColors.TextSecondary
                        )
                    }

                    else -> {
                        Text(
                            text = "Review the transaction details carefully before approving.",
                            style = AppTypography.bodySmall,
                            color = AppColors.TextSecondary
                        )

                        // Warning about phishing
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = AppColors.Warning.copy(alpha = 0.1f)
                            )
                        ) {
                            Text(
                                text = "⚠️ Only approve if you trust this DApp and initiated this action.",
                                style = AppTypography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isProcessing = true
                    onApprove()
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Success
                )
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Approve")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onReject,
                enabled = !isProcessing
            ) {
                Text("Reject")
            }
        },
        modifier = modifier
    )
}

@Composable
fun ConnectionRequestDialog(
    dappUrl: String,
    dappName: String,
    walletName: String,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onReject,
        icon = {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = AppColors.Primary
            )
        },
        title = {
            Text("Connect Wallet", style = AppTypography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "$dappName wants to connect to your wallet.",
                    style = AppTypography.bodyLarge
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Surface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DApp",
                            style = AppTypography.labelSmall,
                            color = AppColors.TextSecondary
                        )
                        Text(extractDomain(dappUrl), fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Wallet",
                            style = AppTypography.labelSmall,
                            color = AppColors.TextSecondary
                        )
                        Text(walletName, fontWeight = FontWeight.Bold)
                    }
                }

                Text(
                    text = "This will allow the DApp to view your public address and request transaction approvals.",
                    style = AppTypography.bodySmall,
                    color = AppColors.TextSecondary
                )
            }
        },
        confirmButton = {
            Button(onClick = onApprove) {
                Text("Connect")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onReject) {
                Text("Cancel")
            }
        }
    )
}

private fun extractDomain(url: String): String {
    return try {
        val uri = java.net.URI(url)
        uri.host ?: url
    } catch (e: Exception) {
        url
    }
}