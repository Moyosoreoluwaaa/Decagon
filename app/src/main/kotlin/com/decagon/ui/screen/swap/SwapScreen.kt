package com.decagon.ui.screen.swap

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import coil3.request.crossfade
import com.decagon.domain.model.TokenInfo
import com.decagon.ui.components.SlippageSettingsSheet
import com.decagon.ui.components.SwapPreviewCard
import com.decagon.ui.components.TokenSelectorSheet
import com.decagon.ui.navigation.UnifiedBottomNavBar
import com.decagon.util.ItemShape
import com.decagon.ui.theme.AppTypography
import com.decagon.util.RoundedShape
import org.koin.androidx.compose.koinViewModel
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
    val isSwapSupported = currentWallet?.activeChain?.chainType?.id == "solana"
    val availableTokens by viewModel.availableTokens.collectAsState()
    val tokensLoading by viewModel.tokensLoading.collectAsState() // ✅ NEW
    val commonTokens by viewModel.commonTokens.collectAsState() // ✅ NEW

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Swap", style = AppTypography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSlippageSettings = true }) {
                        Icon(Icons.Rounded.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = { UnifiedBottomNavBar(navController = navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Wallet Info
            currentWallet?.let { wallet ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ItemShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(wallet.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "${wallet.address.take(4)}...${wallet.address.takeLast(4)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val displayBalance = wallet.activeChain?.balance ?: wallet.balance
                        Text(
                            text = "%.4f SOL".format(displayBalance),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Pay Card
            TokenSelectionCard(
                label = "You Pay",
                token = inputToken,
                amount = inputAmount,
                onAmountChange = { viewModel.onInputAmountChanged(it) },
                onTokenClick = { showInputTokenSelector = true },
                balance = tokenBalances.find { it.mint == inputToken.address }?.uiAmount
            )

            // Swap Switcher Node
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Surface(
                    onClick = { viewModel.onSwapDirectionFlipped() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.SwapVert, "Flip", modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Receive Card
            TokenSelectionCard(
                label = "You Receive",
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

            // Quote Details
            AnimatedVisibility(visible = currentQuote != null) {
                currentQuote?.let { quote ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = ItemShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ) {
                        SwapPreviewCard(quote = quote)
                    }
                }
            }

            // Slippage Shortcut
            Surface(
                onClick = { showSlippageSettings = true },
                color = Color.Transparent,
                shape = ItemShape
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Max Slippage", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "%.2f%%".format(slippageTolerance),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Swap Execution Button
            Button(
                onClick = {
                    if (uiState is SwapUiState.QuoteWithWarnings) showSecurityWarning = true
                    else viewModel.executeSwap()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = ItemShape,
                enabled = uiState is SwapUiState.QuoteReady && isSwapSupported,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (!isSwapSupported) {
                    Text("Switch to Solana to swap")
                } else {
                    SwapButtonLayout(uiState)
                }
            }

            if (uiState is SwapUiState.Error) {
                Text(
                    text = (uiState as SwapUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        }
    }

    // ✅ UPDATED: Input token selector with balances + all available tokens
    if (showInputTokenSelector) {
        // Combine owned tokens with all available tokens, removing duplicates
        val combinedTokens = remember(tokenBalances, availableTokens) {
            val ownedTokenInfo = tokenBalances.mapNotNull { it.toTokenInfo() }
            val allTokens = ownedTokenInfo + availableTokens
            allTokens.distinctBy { it.address }
        }

        TokenSelectorSheet(
            tokens = combinedTokens,
            currentToken = inputToken,
            ownedTokenMints = tokenBalances.map { it.mint }.toSet(),
            commonTokens = commonTokens, // ✅ NEW: Pass common tokens
            isLoading = tokensLoading, // ✅ NEW: Pass loading state
            onTokenSelected = {
                viewModel.onInputTokenSelected(it)
                showInputTokenSelector = false
            },
            onDismiss = { showInputTokenSelector = false }
        )
    }

    // ✅ Output token selector remains the same (all available tokens)
    if (showOutputTokenSelector) {
        TokenSelectorSheet(
            tokens = availableTokens,
            currentToken = outputToken,
            ownedTokenMints = tokenBalances.map { it.mint }.toSet(),
            commonTokens = commonTokens, // ✅ NEW: Pass common tokens
            isLoading = tokensLoading, // ✅ NEW: Pass loading state
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ItemShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                balance?.let {
                    Text("Balance: %.4f".format(it), style = MaterialTheme.typography.labelMedium)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ✅ ENHANCED: Token selector with logo + badges
                Surface(
                    onClick = onTokenClick,
                    shape = RoundedShape,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // ✅ Token Logo with loading state
                        val imageModel = remember(token.logoURI) {
                            coil3.request.ImageRequest.Builder(context = context)
                                .data(token.logoURI ?: "https://via.placeholder.com/24")
                                .crossfade(true)
                                .build()
                        }

                        AsyncImage(
                            model = imageModel,
                            contentDescription = "${token.symbol} logo",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            error = painterResource(com.decagon.R.drawable.ic_launcher_background)
                        )

                        Text(token.symbol.uppercase(), fontWeight = FontWeight.ExtraBold)

                        // Verified Badge
                        if (token.isVerified) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Strict Badge (Jupiter strict list)
                        if (token.isStrict) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                modifier = Modifier.size(16.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "⚡",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                                    )
                                }
                            }
                        }

                        Icon(Icons.Rounded.ArrowDropDown, null)
                    }
                }

                BasicTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    readOnly = readOnly,
                    modifier = Modifier.weight(1f),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterEnd) {
                            if (amount.isEmpty()) {
                                Text(
                                    "0",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.outline,
                                    textAlign = TextAlign.End
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SwapButtonLayout(state: SwapUiState) {
    when (state) {
        is SwapUiState.LoadingQuote -> CircularProgressIndicator(
            Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
        )
        is SwapUiState.ExecutingSwap -> Text("Executing Transaction...")
        is SwapUiState.SwapSuccess -> Text("Swap Successful!")
        else -> Text("Swap Tokens", fontWeight = FontWeight.Bold)
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