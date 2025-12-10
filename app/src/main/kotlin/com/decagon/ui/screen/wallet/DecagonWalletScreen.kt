package com.decagon.ui.screen.wallet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.core.util.DecagonLoadingState
import com.decagon.ui.components.CopyableAddress
import com.decagon.ui.components.DecagonQuickActions
import com.decagon.ui.components.DecagonWalletSelector
import com.decagon.ui.screen.send.DecagonSendSheet
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonWalletScreen(
    viewModel: DecagonWalletViewModel = koinViewModel(),
    onCreateWallet: () -> Unit = {},
    onImportWallet: () -> Unit = {},
    onNavigateToSettings: (String) -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
) {
    val walletState by viewModel.walletState.collectAsState()
    val allWallets by viewModel.allWallets.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = walletState) {
                        is DecagonLoadingState.Success -> {
                            DecagonWalletSelector(
                                currentWallet = state.data,
                                allWallets = allWallets,
                                onWalletSelected = { walletId ->
                                    viewModel.switchWallet(walletId)
                                },
                                onCreateWallet = onCreateWallet,
                                onImportWallet = onImportWallet
                            )
                        }
                        else -> Text("Decagon Wallet")
                    }
                },
                actions = {
                    if (walletState is DecagonLoadingState.Success) {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.History, "Transaction History")
                        }
                    }

                    IconButton(
                        onClick = {
                            val state = walletState
                            if (state is DecagonLoadingState.Success) {
                                onNavigateToSettings(state.data.id)
                            }
                        }
                    ) {
                        when (val state = walletState) {
                            is DecagonLoadingState.Success -> {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = state.data.name.take(1).uppercase(),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }
                            else -> {
                                Icon(Icons.Default.AccountCircle, "Settings")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = walletState) {
            is DecagonLoadingState.Loading -> {
                LoadingView(Modifier.padding(padding))
            }
            is DecagonLoadingState.Success -> {
                WalletContent(
                    wallet = state.data,
                    viewModel = viewModel,
                    modifier = Modifier.padding(padding),
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
private fun WalletContent(
    wallet: com.decagon.domain.model.DecagonWallet,
    viewModel: DecagonWalletViewModel,
    modifier: Modifier = Modifier,
) {
    var showSendSheet by remember { mutableStateOf(false) }
    var showCurrencyMenu by remember { mutableStateOf(false) }
    var showCopiedSnackbar by remember { mutableStateOf(false) }

    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val fiatPrice by viewModel.fiatPrice.collectAsState()

    val clipboardManager = LocalClipboardManager.current

    // Dismiss snackbar after 2s
    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            delay(2000)
            showCopiedSnackbar = false
        }
    }

    // Calculate fiat value
    val fiatValue = wallet.balance * fiatPrice

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = wallet.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CopyableAddress(
                        address = wallet.address,
                        truncated = true,
                        onCopied = { showCopiedSnackbar = true }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {

                            clipboardManager.setText(AnnotatedString(wallet.address))
                            showCopiedSnackbar = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy address",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Chain indicator
                val activeChain = wallet.activeChain
                if (activeChain != null) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = activeChain.chainType.name,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // SOL Balance
                Text(
                    text = "%.4f ${wallet.activeChain?.chainType?.name ?: "SOL"}".format(wallet.balance),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fiat Value with Currency Selector
                Box {
                    Row(
                        modifier = Modifier.clickable { showCurrencyMenu = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatCurrency(fiatValue, selectedCurrency),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Change currency",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Currency dropdown
                    DropdownMenu(
                        expanded = showCurrencyMenu,
                        onDismissRequest = { showCurrencyMenu = false }
                    ) {
                        listOf("usd", "eur", "gbp", "ngn").forEach { currency ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = getCurrencyDisplay(currency),
                                        fontWeight = if (currency == selectedCurrency) {
                                            FontWeight.Bold
                                        } else {
                                            FontWeight.Normal
                                        }
                                    )
                                },
                                onClick = {
                                    viewModel.setCurrency(currency)
                                    showCurrencyMenu = false
                                }
                            )
                        }
                    }
                }

                // Price per SOL
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1 SOL = ${formatCurrency(fiatPrice, selectedCurrency)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DecagonQuickActions(
            wallet = wallet,
            onSendClick = { showSendSheet = true },
            onReceiveClick = { /* show receive dialog */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

//        // In your debug settings or wallet screen
//        Button(onClick = {
//            viewModelScope.launch {
//                val diagnostic = TransactionDiagnostic(
//                    transactionRepository,
//                    rpcClient
//                )
//                diagnostic.diagnoseAndFixPending(wallet.address)
//            }
//        }) {
//            Text("Fix Stuck Transactions")
//        }
    }

    if (showSendSheet) {
        DecagonSendSheet(
            onDismiss = { showSendSheet = false }
        )
    }

    // Snackbar for copy confirmation
    if (showCopiedSnackbar) {
        androidx.compose.material3.Snackbar(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Address copied to clipboard")
        }
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

// Helper functions
private fun formatCurrency(amount: Double, currencyCode: String): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale.US).apply {
            currency = Currency.getInstance(currencyCode.uppercase())
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
        format.format(amount)
    } catch (e: Exception) {
        // Fallback for unsupported currencies
        val symbol = getCurrencySymbol(currencyCode)
        "$symbol%.2f".format(amount)
    }
}

private fun getCurrencySymbol(code: String): String {
    return when (code.lowercase()) {
        "usd" -> "$"
        "eur" -> "€"
        "gbp" -> "£"
        "ngn" -> "₦"
        else -> code.uppercase()
    }
}

private fun getCurrencyDisplay(code: String): String {
    return when (code.lowercase()) {
        "usd" -> "USD ($)"
        "eur" -> "EUR (€)"
        "gbp" -> "GBP (£)"
        "ngn" -> "NGN (₦)"
        else -> code.uppercase()
    }
}