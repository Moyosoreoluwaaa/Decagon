package com.decagon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.decagon.core.util.DecagonQrGenerator
import com.decagon.domain.model.DecagonWallet
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecagonReceiveSheet(
    wallet: DecagonWallet,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    val qrBitmap by remember(wallet.address) {
        derivedStateOf {
            DecagonQrGenerator.generateQrCode(
                address = wallet.address,
                size = 512
            ).getOrNull()
        }
    }
    
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Receive ${wallet.activeChain?.chainType?.name ?: "SOL"}",
                    style = MaterialTheme.typography.titleLarge
                )
                
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // QR Code
            qrBitmap?.let { bitmap ->
                Card(
                    modifier = Modifier.size(280.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Wallet address QR code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }
            } ?: CircularProgressIndicator()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Address
            Text(
                text = "Your ${wallet.activeChain?.chainType?.name} Address",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            CopyableAddress(
                address = wallet.address,
                truncated = false,
                onCopied = { showCopiedSnackbar = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(wallet.address))
                        showCopiedSnackbar = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Copy")
                }
                
                Button(
                    onClick = {
                        // Share QR code
                        qrBitmap?.let { bitmap ->
                            shareQrCode(context, bitmap, wallet.address)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share")
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        if (showCopiedSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Address copied to clipboard")
            }
            
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(2000)
                showCopiedSnackbar = false
            }
        }
    }
}

private fun shareQrCode(
    context: android.content.Context,
    bitmap: android.graphics.Bitmap,
    address: String
) {
    // Save bitmap to cache and share
    Timber.d("Sharing QR code for address: ${address.take(8)}...")
    // TODO: Implement file sharing
}