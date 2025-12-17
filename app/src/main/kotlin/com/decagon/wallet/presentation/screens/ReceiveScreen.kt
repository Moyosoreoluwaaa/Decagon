package com.octane.wallet.presentation.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.octane.wallet.presentation.components.MetallicCard
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.metallicBorder
import com.octane.wallet.presentation.viewmodel.ReceiveEvent
import com.octane.wallet.presentation.viewmodel.ReceiveViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveScreen(
    viewModel: ReceiveViewModel = koinViewModel(),
    selectedSymbol: String = "SOL",
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val receiveState by viewModel.receiveState.collectAsState()
    val context = LocalContext.current

    // Set selected token
    LaunchedEffect(selectedSymbol) {
        viewModel.selectToken(selectedSymbol)
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ReceiveEvent.AddressCopied -> {
                    copyToClipboard(context, event.address)
                    Toast.makeText(context, "Address copied", Toast.LENGTH_SHORT).show()
                }

                is ReceiveEvent.ShareAddress -> {
                    shareAddress(context, event.address)
                }

                is ReceiveEvent.ShareQRCode -> {
                    // TODO: Share QR code image
                }

                is ReceiveEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receive ${receiveState.selectedToken}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.Padding.standard),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.large)
        ) {
            // Instructions
            Text(
                receiveState.instructionMessage,
                style = AppTypography.bodyMedium,
                color = AppColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // QR Code
            MetallicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (receiveState.isGeneratingQR) {
                        CircularProgressIndicator(color = AppColors.SurfaceHighlight)
                    } else {
                        receiveState.qrCodeBitmap?.let { bitmap ->
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(Dimensions.Padding.extraLarge)
                            )
                        }
                    }
                }
            }

            // Address Display
            MetallicCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
                ) {
                    Text(
                        "Your Address",
                        style = AppTypography.labelMedium,
                        color = AppColors.TextSecondary
                    )
                    Text(
                        receiveState.truncatedAddress,
                        style = AppTypography.titleMedium,
                        color = AppColors.TextPrimary
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.medium)
            ) {
                ActionButton(
                    icon = Icons.Rounded.ContentCopy,
                    label = "Copy",
                    onClick = viewModel::copyAddress,
                    modifier = Modifier.weight(1f)
                )

                ActionButton(
                    icon = Icons.Rounded.Share,
                    label = "Share",
                    onClick = viewModel::shareAddress,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(Dimensions.CornerRadius.standard))
            .background(AppColors.Surface)
            .metallicBorder(
                Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.standard),
                135f
            )
            .clickable(onClick = onClick)
            .padding(Dimensions.Padding.standard),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimensions.Spacing.small)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = AppColors.TextPrimary,
            modifier = Modifier.size(Dimensions.IconSize.large)
        )
        Text(
            label,
            style = AppTypography.labelMedium,
            color = AppColors.TextPrimary
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("Address", text))
}

private fun shareAddress(context: Context, address: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, address)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Share Address"))
}