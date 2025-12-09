package com.decagon.ui.screen.chains

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.decagon.core.chains.ChainRegistry
import com.decagon.core.util.DecagonExplorerUtil
import com.decagon.core.util.DecagonLoadingState
import com.decagon.domain.model.ChainWallet
import com.decagon.domain.model.DecagonWallet
import org.koin.androidx.compose.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonSupportedChainsScreen(
    walletId: String,
    onBackClick: () -> Unit,
    onChainSelected: (String) -> Unit,
    viewModel: DecagonSupportedChainsViewModel = koinViewModel()
) {
    val wallet by viewModel.getWallet(walletId).collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supported Chains") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = wallet) {
            is DecagonLoadingState.Success -> {
                ChainList(
                    wallet = state.data,
                    onChainClick = { chainId ->
                        viewModel.switchChain(walletId, chainId)
                        onChainSelected(chainId)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            is DecagonLoadingState.Loading -> {
                Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ChainList(
    wallet: DecagonWallet,
    onChainClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Timber.d("ChainList: Rendering ${wallet.chains.size} chains")
    wallet.chains.forEach { chain ->
        Timber.d("Chain: id=${chain.chainId}, address=${chain.address.take(8)}")
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(wallet.chains) { chainWallet ->
            // Validate chain config exists before rendering
            val configExists = try {
                ChainRegistry.getChain(chainWallet.chainId)
                true
            } catch (e: Exception) {
                Timber.e(e, "Invalid chain: ${chainWallet.chainId}")
                false
            }

            if (configExists) {
                ChainItem(
                    chainWallet = chainWallet,
                    isActive = chainWallet.chainId == wallet.activeChainId,
                    onClick = { onChainClick(chainWallet.chainId) }
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Unsupported chain: ${chainWallet.chainId}",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}


@Composable
private fun ChainItem(
    chainWallet: ChainWallet,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val config = ChainRegistry.getChain(chainWallet.chainId)
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Use Coil for loading remote icon
                AsyncImage(
                    model = config.iconUrl,
                    contentDescription = "${config.nativeCurrency} icon",
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(android.R.drawable.ic_menu_compass),
                    error = painterResource(android.R.drawable.ic_menu_compass)
                )

                Column {
                    Text(
                        text = chainWallet.chainType.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = chainWallet.address.take(8) + "...",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.clickable {
                            DecagonExplorerUtil.openAddress(
                                context,
                                chainWallet.chainId,
                                chainWallet.address
                            )
                        },
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${chainWallet.balance} ${config.symbol}",
                    style = MaterialTheme.typography.titleMedium
                )

                if (isActive) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            "Active",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}