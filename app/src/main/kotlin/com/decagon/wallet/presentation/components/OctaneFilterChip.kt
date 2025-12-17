package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Filter chip with optional dropdown.
 * Used in Discover/Search screens.
 */
@Composable
fun <T> OctaneFilterChip(
    label: String,
    isSelected: Boolean = false,
    hasDropdown: Boolean = false,
    dropdownItems: List<T> = emptyList(),
    onItemSelected: (T) -> Unit = {},
    itemLabel: (T) -> String = { it.toString() },
    onClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(Dimensions.CornerRadius.small))
                .background(if (isSelected) AppColors.SurfaceHighlight else Color.Transparent)
                .clickable {
                    if (hasDropdown) {
                        expanded = true
                    } else {
                        onClick()
                    }
                }
                .padding(
                    horizontal = Dimensions.Padding.medium,
                    vertical = Dimensions.Padding.small
                )
        ) {
            Text(
                text = label,
                style = AppTypography.labelMedium,
                color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary
            )
            
            if (hasDropdown) {
                Spacer(modifier = Modifier.width(Dimensions.Spacing.extraSmall))
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary,
                    modifier = Modifier.size(Dimensions.IconSize.small)
                )
            }
        }
        
        // Dropdown Menu (if applicable)
        if (hasDropdown && dropdownItems.isNotEmpty()) {
            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(AppColors.Surface)
            ) {
                dropdownItems.forEach { item ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = {
                            Text(
                                text = itemLabel(item),
                                style = AppTypography.bodyMedium,
                                color = AppColors.TextPrimary
                            )
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}