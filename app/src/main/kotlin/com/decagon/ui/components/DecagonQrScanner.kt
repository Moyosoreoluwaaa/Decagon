package com.decagon.ui.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import timber.log.Timber

/**
 * QR code scanner button with camera permission handling.
 *
 * Usage:
 * ```
 * DecagonQrScanner(
 *     onAddressScanned = { address ->
 *         viewModel.setRecipient(address)
 *     }
 * )
 * ```
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun DecagonQrScanner(
    onAddressScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    var showPermissionDialog by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        result.contents?.let { scannedData ->
            Timber.d("QR scanned: ${scannedData.take(20)}...")

            // Extract address from various formats
            val address = when {
                // Solana URI: solana:<address>?...
                scannedData.startsWith("solana:") -> {
                    scannedData.substringAfter("solana:")
                        .substringBefore("?")
                }
                // Ethereum/Polygon URI: ethereum:<address>
                scannedData.startsWith("ethereum:") -> {
                    scannedData.substringAfter("ethereum:")
                }
                // Raw address
                else -> scannedData
            }

            onAddressScanned(address)
        } ?: run {
            Timber.w("QR scan cancelled or no data")
        }
    }

    IconButton(
        onClick = {
            if (cameraPermission.status.isGranted) {
                val options = ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Scan wallet address QR code")
                    setBeepEnabled(false)
                    setOrientationLocked(true)
                }
                scanLauncher.launch(options)
            } else {
                showPermissionDialog = true
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.QrCodeScanner,
            contentDescription = "Scan QR code"
        )
    }

    // Permission dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("Camera access is needed to scan QR codes for wallet addresses.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        cameraPermission.launchPermissionRequest()
                        showPermissionDialog = false
                    }
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}