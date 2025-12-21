package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Security warning dialog shown BEFORE displaying seed phrase.
 * Ensures user understands risks before proceeding.
 */
@Composable
fun SeedPhraseSecurityDialog(
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    var acknowledgedItems by remember { mutableStateOf(setOf<Int>()) }
    val allItems = listOf(0, 1, 2) // 3 security warnings
    val allAcknowledged = acknowledgedItems.containsAll(allItems)

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                Icons.Rounded.Warning,
                contentDescription = null,
                tint = AppColors.Error,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                "Critical Security Warning",
                style = AppTypography.headlineMedium,
                color = AppColors.Error
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
            ) {
                Text(
                    "Your seed phrase is the ONLY way to recover your wallet. Please read carefully:",
                    style = AppTypography.bodyMedium,
                    color = AppColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

                // Security checklist
                SecurityCheckItem(
                    index = 0,
                    isChecked = acknowledgedItems.contains(0),
                    onCheckedChange = { checked ->
                        acknowledgedItems = if (checked) {
                            acknowledgedItems + 0
                        } else {
                            acknowledgedItems - 0
                        }
                    },
                    title = "Never share with anyone",
                    description = "Anyone with this phrase can steal ALL your funds. Octane will NEVER ask for it."
                )

                SecurityCheckItem(
                    index = 1,
                    isChecked = acknowledgedItems.contains(1),
                    onCheckedChange = { checked ->
                        acknowledgedItems = if (checked) {
                            acknowledgedItems + 1
                        } else {
                            acknowledgedItems - 1
                        }
                    },
                    title = "Write it down offline",
                    description = "Don't screenshot, copy-paste, or store digitally. Use pen and paper."
                )

                SecurityCheckItem(
                    index = 2,
                    isChecked = acknowledgedItems.contains(2),
                    onCheckedChange = { checked ->
                        acknowledgedItems = if (checked) {
                            acknowledgedItems + 2
                        } else {
                            acknowledgedItems - 2
                        }
                    },
                    title = "Store in a safe place",
                    description = "If you lose this, your funds are GONE FOREVER. No one can recover them."
                )

                Spacer(modifier = Modifier.height(Dimensions.Spacing.small))

                // Warning card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = AppColors.Error.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(Dimensions.Padding.medium),
                        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                    ) {
                        Icon(
                            Icons.Rounded.Warning,
                            contentDescription = null,
                            tint = AppColors.Error,
                            modifier = Modifier.size(Dimensions.IconSize.medium)
                        )
                        Text(
                            "You are solely responsible for keeping your seed phrase safe. There is NO customer support that can recover lost seed phrases.",
                            style = AppTypography.bodySmall,
                            color = AppColors.Error
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onProceed,
                enabled = allAcknowledged,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Error,
                    disabledContainerColor = AppColors.Error.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I Understand, Show My Seed Phrase")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = AppColors.TextSecondary)
            }
        }
    )
}

@Composable
private fun SecurityCheckItem(
    index: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
            .background(
                if (isChecked) AppColors.Success.copy(alpha = 0.1f)
                else AppColors.Surface
            )
            .padding(Dimensions.Padding.medium),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = AppColors.Success,
                uncheckedColor = AppColors.TextSecondary
            )
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = AppTypography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isChecked) AppColors.Success else AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                description,
                style = AppTypography.bodySmall,
                color = AppColors.TextSecondary
            )
        }

        if (isChecked) {
            Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = AppColors.Success,
                modifier = Modifier.size(Dimensions.IconSize.medium)
            )
        }
    }
}