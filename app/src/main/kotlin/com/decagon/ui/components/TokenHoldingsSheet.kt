package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.decagon.domain.model.TokenBalance

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenHoldingsSheet(
    tokens: List<TokenBalance>,
    sheetState: SheetState,
    onManageClick: () -> Unit,
    onTokenClick: (TokenBalance) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = {},
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3A44))
            )
        },
        containerColor = Color(0xFF1A1A24).copy(alpha = 0.95f),
        scrimColor = Color.Black.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tokens",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                TextButton(onClick = onManageClick) {
                    Text(
                        text = "Manage Token List",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF9945FF)
                        )
                    )
                }
            }

            // Empty state
            if (tokens.isEmpty()) {
                EmptyTokensState(modifier = Modifier.padding(vertical = 48.dp))
            } else {
                // Token list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    items(tokens, key = { it.mint }) { token ->
                        TokenRow(
                            token = token,
                            onClick = { onTokenClick(token) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TokenRow(
    token: TokenBalance,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2A2A34).copy(alpha = 0.5f),
                        Color(0xFF1A1A24).copy(alpha = 0.7f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF9945FF).copy(alpha = 0.1f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Token logo
            AsyncImage(
                model = token.logoUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2A34))
            )

            Column {
                Text(
                    text = token.symbol,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
                Text(
                    text = "${"%.4f".format(token.amount)} ${token.symbol}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF7E7E8F)
                    )
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$${"%.2f".format(token.valueUsd)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            )

            if (token.change24h != null) {
                Text(
                    text = "${if (token.change24h >= 0) "+" else ""}${"%.2f".format(token.change24h)}%",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (token.change24h >= 0) Color(0xFF14F195) else Color(0xFFFF6B6B)
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyTokensState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "No tokens yet",
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color(0xFFB4B4C6)
            )
        )
        Text(
            text = "Receive assets to see them here",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF7E7E8F)
            )
        )
    }
}