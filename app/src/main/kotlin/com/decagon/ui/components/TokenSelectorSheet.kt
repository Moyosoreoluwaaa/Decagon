package com.decagon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.TokenInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenSelectorSheet(
    tokens: List<TokenInfo>,
    currentToken: TokenInfo,
    onTokenSelected: (TokenInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredTokens = remember(searchQuery, tokens) {
        if (searchQuery.isBlank()) tokens
        else tokens.filter {
            it.symbol.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f),
        containerColor = Color(0xFF1A1A24),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF3A3A44))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Select Token",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A34).copy(alpha = 0.5f)),
                placeholder = { Text("Search tokens", color = Color(0xFF7E7E8F)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        "Search",
                        tint = Color(0xFF9945FF)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF9945FF),
                    unfocusedBorderColor = Color(0xFF3A3A44),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF9945FF)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredTokens) { token ->
                    TokenListItem(
                        token = token,
                        isSelected = token.address == currentToken.address,
                        onClick = { onTokenSelected(token) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TokenListItem(
    token: TokenInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF9945FF).copy(alpha = 0.2f),
                            Color(0xFF9945FF).copy(alpha = 0.1f)
                        )
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF2A2A34).copy(alpha = 0.5f),
                            Color(0xFF1A1A24).copy(alpha = 0.7f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color(0xFF9945FF).copy(alpha = 0.4f)
                else Color(0xFF3A3A44),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = token.symbol,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = token.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7E7E8F)
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF9945FF))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}