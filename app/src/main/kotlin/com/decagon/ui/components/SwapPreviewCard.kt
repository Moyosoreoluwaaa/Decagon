package com.decagon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.SwapOrder
import com.decagon.domain.model.WarningSeverity
import kotlin.math.pow

@Composable
fun SwapPreviewCard(quote: SwapOrder) {
    val inputToken = quote.inputToken
    val outputToken = quote.outputToken

    // Convert raw amounts to UI amounts
    val outputUiAmount = quote.outAmount.toDoubleOrNull()?.let {
        it / (10.0.pow(outputToken.decimals))
    } ?: 0.0

    val inputUiAmount = quote.inAmount.toDoubleOrNull()?.let {
        it / (10.0.pow(inputToken.decimals))
    } ?: 1.0

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Quote Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            // Expected output (converted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "You receive approx...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "%.4f %s".format(outputUiAmount, outputToken.symbol),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Rate (converted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Exchange Rate", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "1 ${inputToken.symbol} ≈ %.6f ${outputToken.symbol}".format(
                        outputUiAmount / inputUiAmount
                    ),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            // Slippage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Max Slippage", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "%.2f%%".format(quote.slippageBps / 100.0),
                    style = MaterialTheme.typography.titleSmall
                )
            }

            // Price Impact (if significant)
            if (quote.priceImpactPct > 0.01) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Price Impact", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "%.2f%%".format(quote.priceImpactPct),
                        style = MaterialTheme.typography.titleSmall,
                        color = when {
                            quote.priceImpactPct < 1.0 -> MaterialTheme.colorScheme.onSurface
                            quote.priceImpactPct < 3.0 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                }
            }

            // Security warnings indicator
            val criticalWarnings = quote.securityWarnings.values
                .flatten()
                .count { it.severity == WarningSeverity.CRITICAL }

            if (criticalWarnings > 0) {
                HorizontalDivider()
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "$criticalWarnings security warning(s) detected",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
