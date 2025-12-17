package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.components.SeedPhraseSecurityDialog
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeedPhraseDisplayScreen(
    seedPhrase: String,
    walletName: String,
    onConfirm: () -> Unit,
    onBack: () -> Unit
) {
    val words = remember { seedPhrase.split(" ") }
    val clipboardManager = LocalClipboardManager.current
    var showCopyConfirmation by remember { mutableStateOf(false) }
    var hasAcknowledged by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(true) }

    // ✅ Show security dialog with option to proceed
    if (showSecurityDialog) {
        SeedPhraseSecurityDialog(
            onProceed = {
                showSecurityDialog = false
                // ✅ Optionally show seed phrase immediately after proceeding
            },
            onCancel = onBack
        )
    }

    // ✅ Always show scaffold (but blur seed phrase until acknowledged)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup Seed Phrase") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Error
                )
            )
        }
    ) { padding ->
        // ✅ Show content even during security dialog (but blurred)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .then(
                    if (showSecurityDialog) {
                        Modifier.alpha(0.3f) // ✅ Blur during security dialog
                    } else {
                        Modifier
                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Warning Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Error.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = AppColors.Error
                        )
                        Column {
                            Text(
                                text = "Never share your seed phrase!",
                                style = AppTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.Error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Anyone with this phrase can access your funds. Store it securely offline.",
                                style = AppTypography.bodySmall
                            )
                        }
                    }
                }

                // Wallet Info
                Text(
                    text = "Wallet: $walletName",
                    style = AppTypography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Seed Phrase Grid
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(240.dp)
                        ) {
                            itemsIndexed(words) { index, word ->
                                SeedWordChip(number = index + 1, word = word)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Copy Button
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(seedPhrase))
                                showCopyConfirmation = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Rounded.ContentCopy, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy to Clipboard")
                        }
                    }
                }

                // Acknowledgement Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = hasAcknowledged,
                        onCheckedChange = { hasAcknowledged = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I have saved my seed phrase in a secure location",
                        style = AppTypography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Confirm Button
                Button(
                    onClick = onConfirm,
                    enabled = hasAcknowledged,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("I've Saved My Seed Phrase")
                }
            }
        }

        // Copy Confirmation Snackbar
        if (showCopyConfirmation) {
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showCopyConfirmation = false
            }
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showCopyConfirmation = false }) {
                        Text("OK")
                    }
                }
            ) {
                Text("Seed phrase copied to clipboard")
            }
        }
    }
}

@Composable
private fun SeedWordChip(number: Int, word: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = AppColors.Surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$number.",
                style = AppTypography.bodySmall,
                color = AppColors.TextSecondary
            )
            Text(
                text = word,
                style = AppTypography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}