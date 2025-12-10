package com.decagon.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Composable that displays an address with long-press to copy functionality.
 * 
 * @param address Full address string
 * @param truncated Whether to display truncated version
 * @param onCopied Callback when address is copied (for showing toast/snackbar)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CopyableAddress(
    address: String,
    modifier: Modifier = Modifier,
    truncated: Boolean = true,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    onCopied: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val displayText = if (truncated) {
        "${address.take(4)}...${address.takeLast(4)}"
    } else {
        address
    }

    Text(
        text = displayText,
        style = style,
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(10.dp).combinedClickable(
            onClick = { /* Single tap does nothing */ },
            onLongClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                clipboardManager.setText(AnnotatedString(address))
                onCopied()
            }
        )
    )
}