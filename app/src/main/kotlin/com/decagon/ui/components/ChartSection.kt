package com.decagon.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.wallet.core.util.LoadingState

@Composable
fun ChartSection(
    state: LoadingState<List<Double>>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is LoadingState.Loading, is LoadingState.Simulating -> {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
            is LoadingState.Success -> {
                val prices = state.data
                if (prices.isNotEmpty()) {
                    EnhancedPriceChart(
                        prices = prices,
                        modifier = Modifier.fillMaxSize(),
                        // Dynamic color logic: Green if price went up, Red if down
                        lineColor = if (prices.last() >= prices.first())
                            AppColors.Success else AppColors.Error
                    )
                }
            }
            is LoadingState.Error -> {
                Text("Failed to load chart", style = AppTypography.bodySmall)
            }
            else -> { /* Idle state - show nothing or a placeholder */ }
        }
    }
}