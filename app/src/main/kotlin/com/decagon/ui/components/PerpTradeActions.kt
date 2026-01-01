package com.decagon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions


@Composable
fun PerpTradeActions(
    onLong: () -> Unit,
    onShort: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimensions.Spacing.standard)
    ) {
        Button(
            onClick = onLong,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Success),
            shape = RoundedCornerShape(Dimensions.CornerRadius.standard)
        ) {
            Text("Long", style = AppTypography.titleMedium, color = Color.White)
        }

        Button(
            onClick = onShort,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error),
            shape = RoundedCornerShape(Dimensions.CornerRadius.standard)
        ) {
            Text("Short", style = AppTypography.titleMedium, color = Color.White)
        }
    }
}