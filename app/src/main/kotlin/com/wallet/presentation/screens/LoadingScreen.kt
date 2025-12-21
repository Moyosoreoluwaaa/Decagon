package com.octane.wallet.presentation.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.octane.wallet.presentation.theme.AppColors

@Composable
internal fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Companion.Center
    ) {
        CircularProgressIndicator(color = AppColors.TextPrimary)
    }
}