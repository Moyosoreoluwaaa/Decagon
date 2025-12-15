package com.decagon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SlippageSettingsSheet(
    currentSlippage: Double,
    onSlippageChanged: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var localSlippage by remember { mutableFloatStateOf(currentSlippage.toFloat()) }

    ModalBottomSheet(
        onDismissRequest = {
            onSlippageChanged(localSlippage.toDouble())
            onDismiss()
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Slippage Tolerance",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Your transaction will revert if the price changes unfavorably by more than this percentage.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Current value display
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%.2f%%".format(localSlippage),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Slider
            Slider(
                value = localSlippage,
                onValueChange = { localSlippage = it },
                valueRange = 0.1f..5.0f,
                steps = 48, // 0.1% increments
                modifier = Modifier.fillMaxWidth()
            )

            // Range labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("0.1%", style = MaterialTheme.typography.labelSmall)
                Text("5.0%", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Preset buttons
            Text(
                text = "Common Presets",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetButton(
                    value = 0.5f,
                    isSelected = localSlippage == 0.5f,
                    onClick = { localSlippage = 0.5f },
                    modifier = Modifier.weight(1f)
                )
                PresetButton(
                    value = 1.0f,
                    isSelected = localSlippage == 1.0f,
                    onClick = { localSlippage = 1.0f },
                    modifier = Modifier.weight(1f)
                )
                PresetButton(
                    value = 2.0f,
                    isSelected = localSlippage == 2.0f,
                    onClick = { localSlippage = 2.0f },
                    modifier = Modifier.weight(1f)
                )
            }

            // Warning for high slippage
            if (localSlippage > 3.0f) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "⚠ High slippage may result in unfavorable rates",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else if (localSlippage < 0.3f) {
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Low slippage may cause transaction failures",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Apply button
            Button(
                onClick = {
                    onSlippageChanged(localSlippage.toDouble())
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Apply Settings")
            }
        }
    }
}

@Composable
private fun PresetButton(
    value: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Text(
            text = "%.1f%%".format(value),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}