package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.viewmodel.BaseWalletViewModel
import com.octane.wallet.presentation.viewmodel.WalletEvent
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportWalletScreen(
    viewModel: BaseWalletViewModel = koinViewModel(),
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var seedPhrase by remember { mutableStateOf("") }
    var walletName by remember { mutableStateOf("") }
    var showSeedPhrase by remember { mutableStateOf(false) }
    var isImporting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    // Listen to wallet events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is WalletEvent.WalletImported -> {
                    isImporting = false
                    onSuccess()
                }

                is WalletEvent.Error -> {
                    isImporting = false
                    errorMessage = event.message
                }

                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Wallet") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Instructions
            Text(
                text = "Enter your 12 or 24-word seed phrase to restore your wallet.",
                style = AppTypography.bodyLarge,
                color = AppColors.TextSecondary
            )

            // Wallet Name Input
            OutlinedTextField(
                value = walletName,
                onValueChange = { walletName = it },
                label = { Text("Wallet Name") },
                placeholder = { Text("My Imported Wallet") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Seed Phrase Input
            OutlinedTextField(
                value = seedPhrase,
                onValueChange = {
                    seedPhrase = it.lowercase().trim()
                    errorMessage = null
                },
                label = { Text("Seed Phrase") },
                placeholder = { Text("word1 word2 word3 ...") },
                visualTransformation = if (showSeedPhrase) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { showSeedPhrase = !showSeedPhrase }) {
                        Icon(
                            if (showSeedPhrase) Icons.Rounded.VisibilityOff
                            else Icons.Rounded.Visibility,
                            contentDescription = if (showSeedPhrase) "Hide" else "Show"
                        )
                    }
                },
                minLines = 3,
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                isError = errorMessage != null,
                supportingText = errorMessage?.let { { Text(it, color = AppColors.Error) } },
                modifier = Modifier.fillMaxWidth()
            )

            // Validation Info
            val wordCount = seedPhrase.split("\\s+".toRegex())
                .filter { it.isNotBlank() }.size

            Text(
                text = "Words entered: $wordCount ${if (wordCount == 12 || wordCount == 24) "✓" else ""}",
                style = AppTypography.bodySmall,
                color = if (wordCount == 12 || wordCount == 24) {
                    AppColors.Success
                } else {
                    AppColors.TextSecondary
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Import Button
            Button(
                onClick = {
                    val words = seedPhrase.split("\\s+".toRegex())
                        .filter { it.isNotBlank() }

                    when {
                        walletName.isBlank() -> {
                            errorMessage = "Please enter a wallet name"
                        }

                        words.size != 12 && words.size != 24 -> {
                            errorMessage = "Seed phrase must be 12 or 24 words"
                        }

                        else -> {
                            isImporting = true
                            viewModel.importWallet(
                                seedPhrase = words.joinToString(" "),
                                name = walletName
                            )
                        }
                    }
                },
                enabled = !isImporting && walletName.isNotBlank() && wordCount in listOf(12, 24),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Import Wallet")
                }
            }

            // Warning
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = AppColors.Warning.copy(alpha = 0.1f)
                )
            ) {
                Text(
                    text = "⚠️ Never share your seed phrase. We'll encrypt and store it securely on your device.",
                    style = AppTypography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}