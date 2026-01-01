package com.octane.wallet.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.wallet.presentation.components.MetallicCard
import com.octane.wallet.presentation.components.WideActionButton
import com.octane.wallet.presentation.theme.*
import com.octane.wallet.presentation.utils.UiFormatters
import com.octane.wallet.presentation.viewmodel.SendEvent
import com.octane.wallet.presentation.viewmodel.SendViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(
    viewModel: SendViewModel = koinViewModel(),
    prefilledSymbol: String? = null,
    prefilledAddress: String? = null,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sendState by viewModel.sendState.collectAsState()
    val portfolioState by viewModel.portfolioState.collectAsState()
    val context = LocalContext.current

    // Handle prefilled values
    LaunchedEffect(prefilledSymbol) {
        prefilledSymbol?.let { viewModel.selectToken(it) }
    }

    LaunchedEffect(prefilledAddress) {
        prefilledAddress?.let { viewModel.onRecipientChanged(it) }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SendEvent.Success -> onSuccess(event.txHash)
                is SendEvent.Error -> {
                    // TODO: Show toast/snackbar
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Send") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.Padding.standard),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large)
        ) {
            // Token Selector
            TokenSelectorCard(
                selectedToken = sendState.selectedToken,
                balance = sendState.maxBalance,
                onTokenClick = { /* TODO: Show token picker */ }
            )

            // Amount Input
            AmountInputCard(
                amount = sendState.amount,
                amountUsd = sendState.amountUsd,
                onAmountChange = viewModel::onAmountChanged,
                onQuickAmount = viewModel::onQuickAmountClick
            )

            // Recipient Input
            RecipientInputCard(
                recipient = sendState.recipient,
                isValid = sendState.recipientValid,
                onRecipientChange = viewModel::onRecipientChanged
            )

            // Fee Display
            FeeDisplayCard(
                fee = sendState.estimatedFee,
                tokenSymbol = sendState.selectedToken
            )

            Spacer(Modifier.weight(1f))

            // Send Button
            WideActionButton(
                text = if (sendState.isSubmitting) "Sending..." else "Send ${sendState.selectedToken}",
                isPrimary = true,
                enabled = sendState.isValid && !sendState.isSubmitting,
                onClick = { viewModel.onSendClick(context as FragmentActivity) }
            )
        }
    }
}

@Composable
private fun TokenSelectorCard(
    selectedToken: String,
    balance: Double,
    onTokenClick: () -> Unit
) {
    MetallicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTokenClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Token", style = AppTypography.labelMedium, color = AppColors.TextSecondary)
                Text(
                    selectedToken,
                    style = AppTypography.titleMedium,
                    color = AppColors.TextPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("Balance", style = AppTypography.labelMedium, color = AppColors.TextSecondary)
                Text(
                    "%.4f".format(balance),
                    style = AppTypography.titleMedium,
                    color = AppColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun AmountInputCard(
    amount: String,
    amountUsd: Double,
    onAmountChange: (String) -> Unit,
    onQuickAmount: (Float) -> Unit
) {
    MetallicCard {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)) {
            Text("Amount", style = AppTypography.labelMedium, color = AppColors.TextSecondary)

            // Amount Input
            BasicTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = AppTypography.priceDisplay.copy(color = AppColors.TextPrimary),
                modifier = Modifier.fillMaxWidth()
            ) { innerTextField ->
                Box {
                    if (amount.isEmpty()) {
                        Text(
                            "0",
                            style = AppTypography.priceDisplay,
                            color = AppColors.TextTertiary
                        )
                    }
                    innerTextField()
                }
            }

            // USD Value
            Text(
                UiFormatters.formatUsd(amountUsd),
                style = AppTypography.bodyMedium,
                color = AppColors.TextSecondary
            )

            // Quick Amount Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
            ) {
                listOf(
                    "25%" to 0.25f,
                    "50%" to 0.5f,
                    "75%" to 0.75f,
                    "Max" to 1.0f
                ).forEach { (label, percent) ->
                    QuickAmountButton(
                        label = label,
                        onClick = { onQuickAmount(percent) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickAmountButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(Dimensions.CornerRadius.small))
            .background(AppColors.SurfaceHighlight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, style = AppTypography.labelMedium, color = AppColors.TextPrimary)
    }
}

@Composable
private fun RecipientInputCard(
    recipient: String,
    isValid: Boolean,
    onRecipientChange: (String) -> Unit
) {
    MetallicCard {
        Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("To", style = AppTypography.labelMedium, color = AppColors.TextSecondary)
                if (recipient.isNotEmpty()) {
                    Text(
                        if (isValid) "✓ Valid" else "✗ Invalid",
                        style = AppTypography.labelSmall,
                        color = if (isValid) AppColors.Success else AppColors.Error
                    )
                }
            }

            BasicTextField(
                value = recipient,
                onValueChange = onRecipientChange,
                textStyle = AppTypography.bodyMedium.copy(color = AppColors.TextPrimary),
                modifier = Modifier.fillMaxWidth()
            ) { innerTextField ->
                Box {
                    if (recipient.isEmpty()) {
                        Text(
                            "Wallet address or domain",
                            style = AppTypography.bodyMedium,
                            color = AppColors.TextTertiary
                        )
                    }
                    innerTextField()
                }
            }
        }
    }
}

@Composable
private fun FeeDisplayCard(
    fee: Double,
    tokenSymbol: String
) {
    MetallicCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Network Fee", style = AppTypography.bodyMedium, color = AppColors.TextSecondary)
            Text(
                "%.6f %s".format(fee, tokenSymbol),
                style = AppTypography.bodyMedium,
                color = AppColors.TextPrimary
            )
        }
    }
}