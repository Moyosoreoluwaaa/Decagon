package com.decagon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.decagon.ui.theme.AppColors
import com.decagon.ui.theme.AppTypography

@Composable
fun AssetHeader(
    logoUrl: String?,
    symbol: String,
    price: String,
    priceChange: Double,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha)
            .scale(0.9f + (alpha * 0.1f)), // Scale from 0.9 to 1.0 based on alpha
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            AsyncImage(
                model = logoUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(CircleShape)
            )
            Text(text = symbol.uppercase(), style = AppTypography.titleMedium)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = price, style = AppTypography.headlineMedium)
            Text(
                text = "${if (priceChange > 0) "+" else ""}${String.format("%.2f", priceChange)}%",
                color = if (priceChange > 0) AppColors.Success else AppColors.Error,
                style = AppTypography.titleMedium
            )
        }
    }
}