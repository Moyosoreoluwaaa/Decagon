package com.koin.ui.portfolio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.koin.data.coin.TimeRange
import com.koin.domain.portfolio.Portfolio

@Composable
fun PortfolioChart(
    portfolio: Portfolio,
    selectedTimeRange: TimeRange,
    modifier: Modifier = Modifier,
    onEvent: (PortfolioUiEvent) -> Unit,
) {
    Card(
        modifier = modifier.padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            PortfolioPerformanceChart(
                portfolio = portfolio,
                selectedTimeRange = selectedTimeRange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // this is the only fixed height
            )
            TimeRangeSelector(
                selectedTimeRange = selectedTimeRange,
                onTimeRangeSelected = { onEvent(PortfolioUiEvent.SelectTimeRange(it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun TimeRangeSelector(
    selectedTimeRange: TimeRange,
    onTimeRangeSelected: (TimeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(vertical = 2.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        item {
            TimeRange.entries.forEach { range ->
                val isSelected = range == selectedTimeRange
                Button(
                    onClick = { onTimeRangeSelected(range) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 0.dp,
                        pressedElevation = 2.dp
                    ),
                    modifier = modifier
                        .height(32.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    Text(range.displayName)
                }
            }
        }
    }
}