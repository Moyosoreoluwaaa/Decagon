package com.decagon.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.core.util.DecagonQrGenerator
import com.decagon.domain.model.DecagonWallet
import kotlinx.coroutines.delay

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

    LaunchedEffect(showCopiedSnackbar) {
        if (showCopiedSnackbar) {
            delay(2000)
            showCopiedSnackbar = false
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                    text = "Receive ${wallet.activeChain?.chainType?.symbol ?: "SOL"}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        "Close",
                        tint = Color(0xFFB4B4C6)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // QR Code with glassmorphic container
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2A2A34).copy(alpha = 0.6f),
                                Color(0xFF1A1A24).copy(alpha = 0.8f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF9945FF).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                qrBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Wallet address QR code",
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: CircularProgressIndicator(color = Color(0xFF9945FF))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Address label
            Text(
                text = "Your ${wallet.activeChain?.chainType?.symbol ?: "SOL"} Address",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7E7E8F)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Address display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2A2A34).copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = Color(0xFF3A3A44),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = wallet.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(wallet.address))
                        showCopiedSnackbar = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF9945FF)
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF9945FF).copy(alpha = 0.5f),
                                Color(0xFF9945FF).copy(alpha = 0.3f)
                            )
                        )
                    )
                ) {
                    Icon(
                        Icons.Default.ContentCopy,
                        null,
                        Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Copy", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = {
                        qrBitmap?.let { bitmap ->
                            shareQrCode(context, bitmap, wallet.address)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9945FF)
                    )
                ) {
                    Icon(Icons.Default.Share, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share", fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (showCopiedSnackbar) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                containerColor = Color(0xFF2A2A34),
                contentColor = Color.White
            ) {
                Text("Address copied to clipboard")
            }
        }
    }
}

private fun shareQrCode(
    context: android.content.Context,
    bitmap: android.graphics.Bitmap,
    address: String
) {
    // TODO: Implement file sharing
}