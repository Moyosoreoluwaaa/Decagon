package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportWalletBottomSheet(
    onDismiss: () -> Unit,
    onImportWallet: (seedPhrase: String, name: String, emoji: String?, color: String?) -> Unit
) {
    var seedPhrase by remember { mutableStateOf("") }
    var walletName by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf("🔥") }
    var selectedColor by remember { mutableStateOf("#4ECDC4") }
    var isImporting by remember { mutableStateOf(false) }
    var showSeedPhrase by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val wordCount = remember(seedPhrase) {
        seedPhrase.trim().split("\\s+".toRegex())
            .filter { it.isNotBlank() }.size
    }

    val isValidSeedPhrase = wordCount == 12 || wordCount == 24

    val availableEmojis = listOf("🔥", "⚡", "💎", "🚀", "🌟", "🎯", "💰", "🏆")
    val availableColors = listOf(
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#FFA07A",
        "#98D8C8", "#F7DC6F", "#BB8FCE", "#85C1E2"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.Background,
        contentColor = AppColors.TextPrimary,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(Dimensions.Padding.extraLarge)
                .padding(bottom = Dimensions.Padding.large),
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Import Wallet",
                    style = AppTypography.headlineSmall,
                    color = AppColors.TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = AppColors.TextSecondary
                    )
                }
            }

            // Seed Phrase Input
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recovery Phrase (12 or 24 words)",
                        style = AppTypography.labelLarge,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        "$wordCount words",
                        style = AppTypography.labelMedium,
                        color = when {
                            isValidSeedPhrase -> AppColors.Success
                            wordCount > 0 -> AppColors.Warning
                            else -> AppColors.TextTertiary
                        }
                    )
                }

                OutlinedTextField(
                    value = seedPhrase,
                    onValueChange = {
                        seedPhrase = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Enter your 12 or 24 word recovery phrase") },
                    visualTransformation = if (showSeedPhrase)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showSeedPhrase = !showSeedPhrase }) {
                            Icon(
                                if (showSeedPhrase) Icons.Rounded.VisibilityOff
                                else Icons.Rounded.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = AppColors.TextSecondary
                            )
                        }
                    },
                    isError = errorMessage != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isValidSeedPhrase)
                            AppColors.Success else AppColors.BorderDefault,
                        unfocusedBorderColor = AppColors.BorderDefault,
                        errorBorderColor = AppColors.Error,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    )
                )

                if (errorMessage != null) {
                    Text(
                        errorMessage!!,
                        style = AppTypography.bodySmall,
                        color = AppColors.Error
                    )
                }
            }

            HorizontalDivider(color = AppColors.BorderSubtle)

            // Wallet Name
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                Text(
                    "Wallet Name (Optional)",
                    style = AppTypography.labelLarge,
                    color = AppColors.TextSecondary
                )
                OutlinedTextField(
                    value = walletName,
                    onValueChange = { walletName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Imported Wallet") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppColors.Success,
                        unfocusedBorderColor = AppColors.BorderDefault,
                        focusedTextColor = AppColors.TextPrimary,
                        unfocusedTextColor = AppColors.TextPrimary
                    )
                )
            }

            // Emoji Selection
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                Text(
                    "Choose Icon",
                    style = AppTypography.labelLarge,
                    color = AppColors.TextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                ) {
                    availableEmojis.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (emoji == selectedEmoji)
                                        AppColors.SurfaceHighlight
                                    else AppColors.Surface
                                )
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, style = AppTypography.titleMedium)
                        }
                    }
                }
            }

            // Color Selection
            Column(verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)) {
                Text(
                    "Choose Color",
                    style = AppTypography.labelLarge,
                    color = AppColors.TextSecondary
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                ) {
                    availableColors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(color)))
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == selectedColor) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(Dimensions.IconSize.large)
                                )
                            }
                        }
                    }
                }
            }

            // Import Button
            Button(
                onClick = {
                    if (isValidSeedPhrase) {
                        isImporting = true
                        onImportWallet(
                            seedPhrase.trim(),
                            walletName.ifBlank { "Imported Wallet" },
                            selectedEmoji,
                            selectedColor
                        )
                    } else {
                        errorMessage = "Please enter a valid 12 or 24 word recovery phrase"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimensions.Button.heightLarge),
                enabled = isValidSeedPhrase && !isImporting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Success,
                    disabledContainerColor = AppColors.SurfaceHighlight
                )
            ) {
                if (isImporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Dimensions.IconSize.medium),
                        color = Color.White
                    )
                } else {
                    Text(
                        "Import Wallet",
                        style = AppTypography.labelLarge
                    )
                }
            }

            // Warning Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
                    .background(AppColors.WarningContainer)
                    .padding(Dimensions.Padding.standard)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = AppColors.Warning,
                        modifier = Modifier.size(Dimensions.IconSize.medium)
                    )
                    Text(
                        "Never share your recovery phrase with anyone!",
                        style = AppTypography.bodySmall,
                        color = AppColors.Warning
                    )
                }
            }
        }
    }
}