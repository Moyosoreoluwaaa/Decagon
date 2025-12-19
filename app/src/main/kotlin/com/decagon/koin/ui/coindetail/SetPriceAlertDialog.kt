package com.koin.ui.coindetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SetPriceAlertDialog(
    showDialog: Boolean,
    currentPrice: Double,
    priceInput: String,
    coinSymbol: String,
    onPriceChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(text = "Set Price Alert for $coinSymbol") // Hardcoded string
            },
            text = {
                Column {
                    Text(
                        text = "Current Price: ${NumberFormat.getCurrencyInstance(Locale.US).format(currentPrice)}", // Using utility function
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { newValue ->
                            // Simple validation to allow only numbers and a single decimal point
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                                onPriceChange(newValue)
                            }
                        },
                        label = { Text("Target Price (USD)") }, // Hardcoded string
                        leadingIcon = { Text("$") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirm,
                    // Enable only if input is a valid positive number
                    enabled = priceInput.toDoubleOrNull()?.let { it > 0.0 } ?: false
                ) {
                    Text("SET ALERT") // Hardcoded string
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text("CANCEL") // Hardcoded string
                }
            }
        )
    }
}

// Utility function copied from CoinDetailScreen
private fun formatCurrency(amount: Double): String {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
}