package com.decagon.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.decagon.domain.model.SecurityWarning
import com.decagon.domain.model.WarningSeverity

@Composable
fun SecurityWarningDialog(
    warnings: List<SecurityWarning>,
    onDismiss: () -> Unit,
    onProceed: () -> Unit
) {
    val hasCriticalWarnings = warnings.any { it.severity == WarningSeverity.CRITICAL }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Warning,
                null,
                tint = if (hasCriticalWarnings) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.tertiary
                },
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = if (hasCriticalWarnings) "Critical Security Warning" else "Security Notice",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (hasCriticalWarnings) {
                    item {
                        Text(
                            text = "One or more critical issues detected. Proceeding is not recommended.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                items(warnings) { warning ->
                    WarningItem(warning)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onProceed,
                colors = if (hasCriticalWarnings) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Text(if (hasCriticalWarnings) "Proceed Anyway" else "I Understand")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun WarningItem(warning: SecurityWarning) {
    Surface(
        color = when (warning.severity) {
            WarningSeverity.CRITICAL -> MaterialTheme.colorScheme.errorContainer
            WarningSeverity.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            WarningSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
            WarningSeverity.LOW -> MaterialTheme.colorScheme.surfaceVariant
            WarningSeverity.INFO -> MaterialTheme.colorScheme.surfaceVariant
            WarningSeverity.WARNING -> MaterialTheme.colorScheme.error
        },
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Severity badge
            Surface(
                color = when (warning.severity) {
                    WarningSeverity.CRITICAL -> MaterialTheme.colorScheme.error
                    WarningSeverity.HIGH -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    WarningSeverity.MEDIUM -> MaterialTheme.colorScheme.tertiary
                    WarningSeverity.LOW -> MaterialTheme.colorScheme.outline
                    WarningSeverity.INFO -> MaterialTheme.colorScheme.outline
                    WarningSeverity.WARNING -> MaterialTheme.colorScheme.error
                },
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = when (warning.severity) {
                        WarningSeverity.CRITICAL -> "!"
                        WarningSeverity.HIGH -> "!"
                        WarningSeverity.MEDIUM -> "⚠"
                        WarningSeverity.LOW -> "i"
                        WarningSeverity.INFO -> "i"
                        WarningSeverity.WARNING -> "i"
                    },
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = when (warning.severity) {
                        WarningSeverity.CRITICAL, WarningSeverity.HIGH -> MaterialTheme.colorScheme.onError
                        WarningSeverity.MEDIUM -> MaterialTheme.colorScheme.onTertiary
                        WarningSeverity.LOW, WarningSeverity.INFO -> MaterialTheme.colorScheme.surface
                        WarningSeverity.WARNING ->MaterialTheme.colorScheme.error
                    }
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = warning.category,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = warning.description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}