package com.decagon.ui.screen.swap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.decagon.domain.model.TokenInfo
import com.decagon.ui.components.SecurityWarningDialog
import com.decagon.ui.components.SlippageSettingsSheet
import com.decagon.ui.components.SwapPreviewCard
import com.decagon.ui.components.TokenSelectorSheet
import com.decagon.ui.navigation.UnifiedBottomNavBar
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonSwapScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SwapViewModel = koinViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val inputToken by viewModel.inputToken.collectAsState()
    val outputToken by viewModel.outputToken.collectAsState()
    val inputAmount by viewModel.inputAmount.collectAsState()
    val currentQuote by viewModel.currentQuote.collectAsState()
    val slippageTolerance by viewModel.slippageTolerance.collectAsState()
    val currentWallet by viewModel.currentWallet.collectAsState()
    val tokenBalances by viewModel.tokenBalances.collectAsState()

    var showInputTokenSelector by remember { mutableStateOf(false) }
    var showOutputTokenSelector by remember { mutableStateOf(false) }
    var showSlippageSettings by remember { mutableStateOf(false) }
    var showSecurityWarning by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        (context as? FragmentActivity)?.let { activity ->
            viewModel.setActivity(activity)
        }
    }

    LaunchedEffect(uiState) {
        if (uiState is SwapUiState.SwapSuccess) {
            Timber.i("Swap successful!")
            kotlinx.coroutines.delay(3000)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swap") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSlippageSettings = true }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        bottomBar = { UnifiedBottomNavBar(navController = navController) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Wallet Balance (Flat card)
                currentWallet?.let { wallet ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .clickable { }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = wallet.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${wallet.address.take(4)}...${wallet.address.takeLast(4)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            val displayBalance = wallet.activeChain?.balance ?: wallet.balance
                            val chainSymbol = wallet.activeChain?.chainType?.symbol ?: "SOL"

                            Text(
                                text = "%.4f %s".format(displayBalance, chainSymbol),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Input Token (Flat design)
                TokenSelectionCard(
                    label = "You pay",
                    token = inputToken,
                    amount = inputAmount,
                    onAmountChange = { viewModel.onInputAmountChanged(it) },
                    onTokenClick = { showInputTokenSelector = true },
                    balance = tokenBalances.find { it.mint == inputToken.address }?.uiAmount
                )

                // Swap Direction Button
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    FilledIconButton(
                        onClick = { viewModel.onSwapDirectionFlipped() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.SwapVert, "Flip", modifier = Modifier.size(20.dp))
                    }
                }

                // Output Token (Flat design)
                TokenSelectionCard(
                    label = "You receive",
                    token = outputToken,
                    amount = currentQuote?.let { quote ->
                        val outputUiAmount = quote.outAmount.toDoubleOrNull()?.let {
                            it / (10.0.pow(outputToken.decimals))
                        } ?: 0.0
                        "%.6f".format(outputUiAmount)
                    } ?: "",
                    onAmountChange = {},
                    onTokenClick = { showOutputTokenSelector = true },
                    balance = tokenBalances.find { it.mint == outputToken.address }?.uiAmount,
                    readOnly = true
                )

                // Quote Display (Simplified)
                AnimatedVisibility(visible = currentQuote != null) {
                    currentQuote?.let { quote ->
                        SwapPreviewCard(quote = quote)
                    }
                }

                // Slippage Info (Minimal)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .clickable { showSlippageSettings = true }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Slippage",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "%.2f%%".format(slippageTolerance),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Default.ChevronRight,
                                null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Swap Button
                Button(
                    onClick = {
                        if (uiState is SwapUiState.QuoteWithWarnings) {
                            showSecurityWarning = true
                        } else {
                            viewModel.executeSwap()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = when (uiState) {
                        is SwapUiState.QuoteReady,
                        is SwapUiState.QuoteWithWarnings -> true
                        else -> false
                    }
                ) {
                    when (uiState) {
                        SwapUiState.Idle -> Text("Enter amount")
                        SwapUiState.LoadingQuote -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        is SwapUiState.QuoteReady,
                        is SwapUiState.QuoteWithWarnings -> Text("Review Swap")
                        SwapUiState.ExecutingSwap -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        is SwapUiState.SwapSuccess -> Text("✓ Success")
                        is SwapUiState.Error -> Text("Try Again")
                    }
                }

                // Error Message (Flat)
                if (uiState is SwapUiState.Error) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = (uiState as SwapUiState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Loading Overlay
            if (uiState is SwapUiState.ExecutingSwap) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Executing swap...", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }

    // Bottom Sheets (keep as-is)
    if (showInputTokenSelector) {
        TokenSelectorSheet(
            tokens = tokenBalances.mapNotNull { it.toTokenInfo() },
            currentToken = inputToken,
            onTokenSelected = {
                viewModel.onInputTokenSelected(it)
                showInputTokenSelector = false
            },
            onDismiss = { showInputTokenSelector = false }
        )
    }

    if (showOutputTokenSelector) {
        TokenSelectorSheet(
            tokens = tokenBalances.mapNotNull { it.toTokenInfo() },
            currentToken = outputToken,
            onTokenSelected = {
                viewModel.onOutputTokenSelected(it)
                showOutputTokenSelector = false
            },
            onDismiss = { showOutputTokenSelector = false }
        )
    }

    if (showSlippageSettings) {
        SlippageSettingsSheet(
            currentSlippage = slippageTolerance,
            onSlippageChanged = { viewModel.onSlippageToleranceChanged(it) },
            onDismiss = { showSlippageSettings = false }
        )
    }

    if (showSecurityWarning && currentQuote != null) {
        SecurityWarningDialog(
            warnings = currentQuote!!.securityWarnings.values.flatten(),
            onDismiss = { showSecurityWarning = false },
            onProceed = {
                showSecurityWarning = false
                viewModel.executeSwap()
            }
        )
    }
}

@Composable
private fun TokenSelectionCard(
    label: String,
    token: TokenInfo,
    amount: String,
    onAmountChange: (String) -> Unit,
    onTokenClick: () -> Unit,
    balance: Double?,
    readOnly: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Label + Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                balance?.let {
                    Text(
                        text = "%.4f".format(it),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Token + Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Token Selector (Flat button)
                TextButton(onClick = onTokenClick) {
                    Text(
                        text = token.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Amount Input
                if (!readOnly) {
                    BasicTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        textStyle = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.End
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier.width(140.dp),
                        singleLine = true,
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterEnd) {
                                if (amount.isEmpty()) {
                                    Text(
                                        "0.0",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                } else {
                    Text(
                        text = if (amount.isNotBlank()) amount else "0.0",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun com.decagon.domain.model.TokenBalance.toTokenInfo(): TokenInfo? {
    return tokenInfo ?: TokenInfo(
        address = mint,
        symbol = symbol,
        name = name,
        decimals = decimals,
        logoURI = null
    )
}