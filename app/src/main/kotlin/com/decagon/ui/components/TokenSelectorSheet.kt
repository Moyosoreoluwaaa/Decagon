package com.decagon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.crossfade
import com.decagon.domain.model.TokenInfo
import com.decagon.util.ItemShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenSelectorSheet(
    tokens: List<TokenInfo>,
    currentToken: TokenInfo,
    ownedTokenMints: Set<String> = emptySet(),
    commonTokens: List<TokenInfo> = emptyList(), // ✅ NEW: Common tokens for quick access
    isLoading: Boolean = false, // ✅ NEW: Loading state
    onTokenSelected: (TokenInfo) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // ✅ ENHANCED: Sort with common tokens section
    val filteredTokens = remember(searchQuery, tokens, ownedTokenMints, commonTokens) {
        val filtered = if (searchQuery.isBlank()) tokens
        else tokens.filter {
            it.symbol.contains(searchQuery, ignoreCase = true) ||
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.address.contains(searchQuery, ignoreCase = true)
        }

        // Sort: Owned first → Verified → Strict → Rest
        filtered.sortedWith(
            compareByDescending<TokenInfo> { it.address in ownedTokenMints }
                .thenByDescending { it.isVerified }
                .thenByDescending { it.isStrict }
                .thenBy { it.symbol }
        )
    }

    // ✅ NEW: Get top 3 owned tokens for quick access
    val quickAccessTokens = remember(ownedTokenMints, filteredTokens) {
        filteredTokens.filter { it.address in ownedTokenMints }.take(3)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Select Token",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(ItemShape),
                placeholder = {
                    Text("Search by name, symbol, or address",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium)
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ✅ Loading state
            if (isLoading && tokens.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Loading tokens...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@Column
            }

            // ✅ NEW: Quick Access Section (Top 3 owned tokens)
            if (quickAccessTokens.isNotEmpty() && searchQuery.isBlank()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Quick Access",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAccessTokens.forEach { token ->
                            QuickAccessTokenChip(
                                token = token,
                                isSelected = token.address == currentToken.address,
                                onClick = { onTokenSelected(token) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // ✅ Section Headers for Owned Tokens
            val hasOwnedTokens = filteredTokens.any { it.address in ownedTokenMints }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (hasOwnedTokens && searchQuery.isBlank()) {
                    item {
                        Text(
                            text = "Your Tokens",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                items(filteredTokens) { token ->
                    val isOwned = token.address in ownedTokenMints

                    TokenListItem(
                        token = token,
                        isSelected = token.address == currentToken.address,
                        isOwned = isOwned,
                        onClick = { onTokenSelected(token) }
                    )

                    // ✅ Add divider after last owned token
                    if (isOwned && searchQuery.isBlank()) {
                        val nextToken = filteredTokens.getOrNull(
                            filteredTokens.indexOf(token) + 1
                        )
                        if (nextToken?.address !in ownedTokenMints) {
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "All Tokens",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ✅ NEW: Quick Access Token Chip Component
@Composable
private fun QuickAccessTokenChip(
    token: TokenInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                MaterialTheme.colorScheme.primary
            )
        } else null
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val context = LocalContext.current
            // Token Logo
            val imageModel = remember(token.logoURI) {
                coil3.request.ImageRequest.Builder(context)
                    .data(token.logoURI ?: "https://via.placeholder.com/32")
                    .crossfade(true)
                    .build()
            }

            AsyncImage(
                model = imageModel,
                contentDescription = "${token.symbol} logo",
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                error = painterResource(com.decagon.R.drawable.ic_launcher_background)
            )

            // Token Symbol
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = token.symbol,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                if (token.isVerified) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp)
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
    isOwned: Boolean, // ✅ NEW: Highlight owned tokens
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ItemShape,
        color = when {
            isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            isOwned -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            // ✅ Token Logo with proper caching
            val imageModel = remember(token.logoURI) {
                coil3.request.ImageRequest.Builder(context)
                    .data(token.logoURI ?: "https://via.placeholder.com/40")
                    .crossfade(true)
                    .build()
            }

            AsyncImage(
                model = imageModel,
                contentDescription = "${token.symbol} logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                error = painterResource(com.decagon.R.drawable.ic_launcher_background)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = token.symbol.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    // ✅ Verified Badge
                    if (token.isVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // ✅ Strict List Badge (Jupiter curated)
                    if (token.isStrict) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    text = "Strict",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // ✅ Owned Badge
                    if (isOwned) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Owned",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

//                // ✅ Show token address truncated
//                Text(
//                    text = "${token.address.take(4)}...${token.address.takeLast(4)}",
//                    style = MaterialTheme.typography.labelSmall,
//                    color = MaterialTheme.colorScheme.outline,
//                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
//                )
            }

            // ✅ Selected Checkmark
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}