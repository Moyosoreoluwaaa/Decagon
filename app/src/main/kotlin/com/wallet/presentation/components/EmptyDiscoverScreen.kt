package com.octane.wallet.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import timber.log.Timber

@Composable
internal fun EmptyDiscoverScreen(
    message: String,
    modifier: Modifier = Modifier
) {
    Timber.d("🎨 Rendering EmptyDiscoverScreen: $message")
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(Dimensions.Padding.large),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            message,
            style = AppTypography.bodyMedium,
            color = AppColors.TextSecondary
        )
    }
}
