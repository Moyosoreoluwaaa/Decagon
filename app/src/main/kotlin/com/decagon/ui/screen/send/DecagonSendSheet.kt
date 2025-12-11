package com.decagon.ui.screen.send

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonTransaction
import com.decagon.ui.components.DecagonQrScanner // Assuming this is correct
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonSendSheet(
    onDismiss: () -> Unit,
    viewModel: DecagonSendViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    DisposableEffect(activity) {
        activity?.let { viewModel.setActivity(it) }
        onDispose {
            viewModel.setActivity(null)
            viewModel.resetState()
        }
    }

    val sendState by viewModel.sendState.collectAsState()
    var toAddress by rememberSaveable { mutableStateOf("") } // Use rememberSaveable for the primary state
    var amount by rememberSaveable { mutableStateOf("") } // Use rememberSaveable for the primary state

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Send SOL",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Content based on state
            when (val state = sendState) {
                is DecagonLoadingState.Idle -> {
                    SendForm(
                        toAddress = toAddress,
                        onToAddressChange = { toAddress = it },
                        amount = amount,
                        onAmountChange = { amount = it },
                        onSend = {
                            Timber.d("Send button clicked")
                            // This ensures the action is run on the main thread for Android API interactions
                            activity?.window?.decorView?.post {
                                viewModel.sendToken(toAddress, amount.toDoubleOrNull() ?: 0.0)
                            }
                        }
                    )
                }

                is DecagonLoadingState.Loading -> {
                    LoadingView()
                }

                is DecagonLoadingState.Success -> {
                    SuccessView(
                        transaction = state.data,
                        onDone = onDismiss
                    )
                }

                is DecagonLoadingState.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { viewModel.resetState() }
                    )
                }
            }
        }
    }
}

@Composable
private fun SendForm(
    toAddress: String,
    onToAddressChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    // ❌ Removed the unused local state: var recipientAddress by rememberSaveable { mutableStateOf("") }
    // ❌ Removed the unused lambda: onScanQr: () -> Unit

    OutlinedTextField(
        value = toAddress,
        onValueChange = onToAddressChange,
        label = { Text("Recipient Address") },
        placeholder = { Text("Solana address") },
        // ✅ CORRECTED: Use DecagonQrScanner directly as the trailing icon, and
        // bind its result directly to the parent state update function (onToAddressChange).
        trailingIcon = {
            DecagonQrScanner(
                onAddressScanned = onToAddressChange // 👈 This is the key fix!
            )
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text("Amount (SOL)") },
        placeholder = { Text("0.0") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSend,
        modifier = Modifier.fillMaxWidth(),
        enabled = toAddress.isNotBlank() && amount.toDoubleOrNull() != null
    ) {
        Text("Send")
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun LoadingView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Sending transaction...")
    }
}

@Composable
private fun SuccessView(
    transaction: DecagonTransaction,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✓ Transaction Sent",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                DetailRow("Amount", "${transaction.amount} SOL")
                DetailRow("To", transaction.to.take(8) + "...")
                transaction.truncatedSignature?.let {
                    DetailRow("Signature", it)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Transaction Failed",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onRetry) {
            Text("Try Again")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SuccessContent(transaction: DecagonTransaction) {
    Card {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.CheckCircle, null, tint = Color.Green)

            Text("Transaction Sent!", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(16.dp))

            DetailRow("Amount", "${transaction.amount} SOL")
            DetailRow("Base Fee", "${transaction.fee / 1_000_000_000.0} SOL")

            if (transaction.priorityFee > 0) {
                DetailRow("Priority Fee", "${transaction.priorityFee / 1_000_000_000.0} SOL")
            }

            DetailRow("Total", "${transaction.amount + transaction.totalFeeSol} SOL")

            Text(
                text = "Signature: ${transaction.truncatedSignature}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}