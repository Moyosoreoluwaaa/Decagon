package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.octane.wallet.domain.models.Wallet
import com.octane.wallet.presentation.theme.*
import com.octane.wallet.presentation.utils.metallicBorder

/**
 * Edit Wallet Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWalletBottomSheet(
    wallet: Wallet,
    onDismiss: () -> Unit,
    onUpdateWallet: (name: String, emoji: String?, color: String?) -> Unit
) {
    var name by remember { mutableStateOf(wallet.name) }
    var selectedEmoji by remember { mutableStateOf(wallet.iconEmoji ?: "💼") }
    var selectedColor by remember {
        mutableStateOf(
            wallet.colorHex?.let { Color(android.graphics.Color.parseColor(it)) }
                ?: AppColors.Primary
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Edit Wallet", style = AppTypography.headlineMedium)

            // Name input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Wallet Name") },
                modifier = Modifier.fillMaxWidth()
            )

            // Emoji selector (grid of common emojis)
            Text("Icon", style = AppTypography.titleSmall)
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.height(120.dp)
            ) {
                items(listOf("💼", "🔥", "🚀", "💎", "⚡", "🌟")) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (emoji == selectedEmoji) AppColors.Primary else Color.Transparent)
                            .clickable { selectedEmoji = emoji },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, style = AppTypography.headlineMedium)
                    }
                }
            }

            // Color picker (predefined palette)
            Text("Color", style = AppTypography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    AppColors.Success,
                    AppColors.Primary,
                    AppColors.Warning,
                    Color(0xFFE91E63)
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(color)
                            .clickable { selectedColor = color }
                            .then(
                                if (color == selectedColor) {
                                    Modifier.border(3.dp, AppColors.TextPrimary, CircleShape)
                                } else Modifier
                            )
                    )
                }
            }

            Button(
                onClick = {
                    onUpdateWallet(
                        name,
                        selectedEmoji,
                        String.format("#%06X", 0xFFFFFF and selectedColor.toArgb())
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Changes")
            }
        }
    }
}

/**
 * Delete Confirmation Dialog
 */
@Composable
fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(Dimensions.CornerRadius.extraLarge))
                .background(AppColors.Surface)
                .metallicBorder(
                    Dimensions.Border.standard,
                    RoundedCornerShape(Dimensions.CornerRadius.extraLarge),
                    angleDeg = 135f
                )
                .padding(Dimensions.Padding.extraLarge)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error Icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(AppColors.ErrorContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.DeleteForever,
                        contentDescription = null,
                        tint = AppColors.Error,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Title
                Text(
                    "Delete Wallet?",
                    style = AppTypography.headlineMedium,
                    color = AppColors.TextPrimary
                )

                // Description
                Text(
                    "This action cannot be undone. Make sure you have backed up your recovery phrase.",
                    style = AppTypography.bodyMedium,
                    color = AppColors.TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                // Warning Box
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
                            "All wallet data will be permanently deleted",
                            style = AppTypography.bodySmall,
                            color = AppColors.Warning
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimensions.Button.heightMedium)
                    ) {
                        Text("Cancel", style = AppTypography.labelLarge)
                    }

                    // Delete Button
                    Button(
                        onClick = {
                            onConfirm()
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimensions.Button.heightMedium),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Error
                        )
                    ) {
                        Text("Delete", style = AppTypography.labelLarge)
                    }
                }
            }
        }
    }
}