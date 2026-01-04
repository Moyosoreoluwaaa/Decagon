package com.decagon.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
fun TimeoutPickerDialog(
    currentTimeout: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    val timeouts = listOf(
        60 to "1 minute",
        300 to "5 minutes",
        900 to "15 minutes",
        1800 to "30 minutes",
        3600 to "1 hour"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Auto-Lock Timeout") },
        text = {
            Column {
                timeouts.forEach { (seconds, label) ->
                    TextButton(
                        onClick = { onSelect(seconds) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            label,
                            color = if (seconds == currentTimeout) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// In CurrencyPickerDialog.kt (create if doesn't exist)
@Composable
fun CurrencyPickerDialog(
    currentCurrency: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val currencies = listOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            Column {
                currencies.forEach { currency ->
                    TextButton(
                        onClick = { onSelect(currency) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            currency,
                            color = if (currency == currentCurrency) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Wallet Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Wallet Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

