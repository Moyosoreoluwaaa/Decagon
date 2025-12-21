package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions
import com.octane.wallet.presentation.utils.metallicBorder

@Composable
internal fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(Dimensions.Button.heightMedium)
            .clip(RoundedCornerShape(Dimensions.CornerRadius.large))
            .background(
                if (isSelected) AppColors.SurfaceHighlight else AppColors.Surface
            )
            .metallicBorder(
                if (isSelected) Dimensions.Border.thick else Dimensions.Border.standard,
                RoundedCornerShape(Dimensions.CornerRadius.large),
                angleDeg = 135f
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = AppTypography.labelLarge,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary
        )
    }
}

