package com.decagon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.SwapOrder
import com.decagon.domain.model.WarningSeverity

@Composable
fun SwapPreviewCard(quote: SwapOrder) {
    val inputToken = quote.inputToken
    val outputToken = quote.outputToken

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Quote Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider()

            // Expected output
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "You receive approximately",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${quote.expectedOutputAmount} ${outputToken.symbol}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Exchange Rate", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "1 ${inputToken.symbol} ≈ %.6f ${outputToken.symbol}".format(
                        quote.expectedOutputAmount.toDoubleOrNull()?.div(
                            quote.inAmount.toDoubleOrNull() ?: 1.0
                        ) ?: 0.0
                    ),
                    style = MaterialTheme.typography.bodyMedium
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
                    style = MaterialTheme.typography.bodyMedium
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
                        style = MaterialTheme.typography.bodyMedium,
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

//@Composable
//private fun DetailRow(
//    label: String,
//    value: String,
//    highlight: Boolean = false,
//    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
//    indent: Boolean = false
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(start = if (indent) 16.dp else 0.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Text(
//            text = label,
//            style = MaterialTheme.typography.bodyMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Text(
//            text = value,
//            style = if (highlight) {
//                MaterialTheme.typography.titleMedium
//            } else {
//                MaterialTheme.typography.bodyMedium
//            },
//            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
//            color = valueColor
//        )
//    }
//}
//
//@Composable
//private fun RouteInfo(quote: SwapOrder) {
//    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//        Text(
//            text = "Route",
//            style = MaterialTheme.typography.labelMedium,
//            color = MaterialTheme.colorScheme.onSurfaceVariant
//        )
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            quote.routePlan.forEachIndexed { index, step ->
//                Text(
//                    text = step.token.symbol,
//                    style = MaterialTheme.typography.bodySmall
//                )
//                if (index < quote.routePlan.size - 1) {
//                    Text(" → ", style = MaterialTheme.typography.bodySmall)
//                }
//            }
//        }
//    }
//}