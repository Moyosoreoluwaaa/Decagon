package com.decagon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppTypography

@Composable
fun TimeframeRow(
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeframes = listOf("1H", "1D", "1W", "1M", "1Y", "ALL")
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        timeframes.forEach { timeframe ->
            val isSelected = selectedTimeframe == timeframe
            
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onTimeframeSelected(timeframe) },
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                        else Color.Transparent,
                shape = CircleShape
            ) {
                Text(
                    text = timeframe,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = AppTypography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer 
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}