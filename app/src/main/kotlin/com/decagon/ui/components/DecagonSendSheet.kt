package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.DecagonTransaction
import com.decagon.ui.screen.send.DecagonSendViewModel
import org.koin.androidx.compose.koinViewModel

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
    var toAddress by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1A1A24),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3A44))
            )
        }
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
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        "Close",
                        tint = Color(0xFFB4B4C6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = sendState) {
                is DecagonLoadingState.Idle -> {
                    SendForm(
                        toAddress = toAddress,
                        onToAddressChange = { toAddress = it },
                        amount = amount,
                        onAmountChange = { amount = it },
                        onSend = {
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
    OutlinedTextField(
        value = toAddress,
        onValueChange = onToAddressChange,
        label = { Text("Recipient Address", color = Color(0xFFB4B4C6)) },
        placeholder = { Text("Solana address", color = Color(0xFF7E7E8F)) },
        trailingIcon = {
            DecagonQrScanner(onAddressScanned = onToAddressChange)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A34).copy(alpha = 0.5f)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF9945FF),
            unfocusedBorderColor = Color(0xFF3A3A44),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF9945FF)
        ),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = amount,
        onValueChange = onAmountChange,
        label = { Text("Amount (SOL)", color = Color(0xFFB4B4C6)) },
        placeholder = { Text("0.0", color = Color(0xFF7E7E8F)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A34).copy(alpha = 0.5f)),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF9945FF),
            unfocusedBorderColor = Color(0xFF3A3A44),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            cursorColor = Color(0xFF9945FF)
        ),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
        onClick = onSend,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp)),
        enabled = toAddress.isNotBlank() && amount.toDoubleOrNull() != null,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF9945FF),
            disabledContainerColor = Color(0xFF3A3A44)
        )
    ) {
        Text(
            "Send",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
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
        CircularProgressIndicator(color = Color(0xFF9945FF))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Sending transaction...",
            color = Color(0xFFB4B4C6),
            style = MaterialTheme.typography.bodyMedium
        )
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
        Icon(
            Icons.Default.CheckCircle,
            null,
            tint = Color(0xFF14F195),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Transaction Sent",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A34).copy(alpha = 0.5f),
                            Color(0xFF1A1A24).copy(alpha = 0.7f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFF9945FF).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailRow("Amount", "${transaction.amount} SOL")
                DetailRow("To", "${transaction.to.take(8)}...")
                transaction.truncatedSignature?.let {
                    DetailRow("Signature", it)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9945FF)
            )
        ) {
            Text(
                "Done",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
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
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B6B)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB4B4C6)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp)),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF9945FF)
            )
        ) {
            Text("Try Again")
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF7E7E8F)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
}