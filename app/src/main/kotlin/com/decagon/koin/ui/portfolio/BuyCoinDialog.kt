package com.koin.ui.portfolio

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.koin.domain.model.Coin
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BuyCoinDialog(
    coin: Coin?,
    availableBalance: Double,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Double, totalCost: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    if (coin == null) return

    var quantityText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val quantity = quantityText.toDoubleOrNull() ?: 0.0
    val totalCost = quantity * coin.currentPrice
    val transactionFee = totalCost * 0.001 // 0.1% transaction fee
    val totalWithFees = totalCost + transactionFee

    val isValidQuantity = quantity > 0
    val canAfford = totalWithFees <= availableBalance
    val isValidTransaction = isValidQuantity && canAfford && !isLoading

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buy ${coin.symbol.uppercase()}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Coin info
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = coin.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Current Price: ${coin.formattedPrice}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Available Balance: ${NumberFormat.getCurrencyInstance(Locale.US).format(availableBalance)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quantity input
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { 
                        quantityText = it
                        errorMessage = null
                    },
                    label = { Text("Quantity") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick amount buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickAmounts = listOf(25.0, 50.0, 100.0, 250.0)
                    quickAmounts.forEach { amount ->
                        val maxQuantity = (availableBalance * 0.999) / coin.currentPrice // Account for fees
                        val quickQuantity = amount / coin.currentPrice
                        val actualQuantity = if (quickQuantity <= maxQuantity) quickQuantity else maxQuantity
                        
                        OutlinedButton(
                            onClick = { 
                                if (actualQuantity > 0) {
                                    quantityText = String.format(Locale.US, "%.6f", actualQuantity)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = amount <= availableBalance
                        ) {
                            Text("$${amount.toInt()}")
                        }
                    }
                }

                // Transaction summary
                if (isValidQuantity) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Transaction Summary",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            TransactionRow("Quantity:", String.format(Locale.US, "%.6f %s", quantity, coin.symbol.uppercase()))
                            TransactionRow("Price per coin:", coin.formattedPrice)
                            TransactionRow("Subtotal:", NumberFormat.getCurrencyInstance(Locale.US).format(totalCost))
                            TransactionRow("Transaction fee:", NumberFormat.getCurrencyInstance(Locale.US).format(transactionFee))
                            
                            Divider()
                            
                            TransactionRow(
                                "Total:", 
                                NumberFormat.getCurrencyInstance(Locale.US).format(totalWithFees),
                                isTotal = true
                            )
                            
                            if (!canAfford) {
                                Text(
                                    text = "Insufficient balance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (isValidTransaction) {
                                isLoading = true
                                onConfirm(quantity, totalWithFees)
                            } else {
                                when {
                                    !isValidQuantity -> errorMessage = "Please enter a valid quantity"
                                    !canAfford -> errorMessage = "Insufficient balance"
                                }
                            }
                        },
                        enabled = isValidTransaction,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Buy")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(
    label: String,
    value: String,
    isTotal: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = if (isTotal) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = if (isTotal) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isTotal) FontWeight.Bold else FontWeight.Normal,
            color = if (isTotal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
