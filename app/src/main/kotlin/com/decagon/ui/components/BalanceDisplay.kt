package com.decagon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
private fun BalanceDisplay(
    balance: Double,
    changeAmount: Double,
    changePercent: Double,
    walletAddress: String,
    onAddressCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .drawBehind {
                // Radial glow behind balance
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF9945FF).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2, size.height / 3),
                        radius = size.width * 0.6f
                    )
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Truncated address with copy
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable(onClick = onAddressCopy)
        ) {
            Text(
                text = "${walletAddress.take(4)}...${walletAddress.takeLast(4)}",
                style = MaterialTheme.typography.bodyMedium.copy(
//                    fontFamily = IBMPlexMono, // Technical precision
                    color = Color(0xFF7E7E8F)
                )
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = Color(0xFF7E7E8F),
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dramatic balance typography
        Text(
            text = "$${"%.2f".format(balance)}",
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
//                fontFamily = Inter,
                letterSpacing = (-1).sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Change indicator
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = if (changeAmount >= 0) Color(0xFF14F195) else Color(0xFFFF6B6B)
                    )
                ) {
                    append(if (changeAmount >= 0) "+" else "")
                    append("${"%.2f".format(changeAmount)}")
                }
                append(" ")
                withStyle(
                    style = SpanStyle(color = Color(0xFFB4B4C6))
                ) {
                    append("(${"%.2f".format(changePercent)}%) Today")
                }
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}