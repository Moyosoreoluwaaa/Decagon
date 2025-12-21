package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography

@Composable
fun EmptyActivityScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration (use Lottie or SVG)
        Icon(
            Icons.Rounded.Receipt,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = AppColors.TextSecondary.copy(alpha = 0.3f)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "No Transactions Yet",
            style = AppTypography.headlineMedium,
            color = AppColors.TextPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Your transaction history will appear here\nafter your first transfer",
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        // CTA
        Button(onClick = { /* Navigate to Send */ }) {
            Text("Send Your First Transaction")
        }
    }
}