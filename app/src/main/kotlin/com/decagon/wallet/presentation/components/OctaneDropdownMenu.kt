package com.octane.wallet.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.octane.wallet.presentation.theme.AppColors
import com.octane.wallet.presentation.theme.AppTypography
import com.octane.wallet.presentation.theme.Dimensions

/**
 * Custom dropdown menu with metallic styling.
 */
@Composable
fun <T> OctaneDropdownMenu(
    selectedItem: T,
    items: List<T>,
    onItemSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    itemLabel: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }
    
    Column(modifier = modifier) {
        // Label
        if (label != null) {
            Text(
                text = label,
                style = AppTypography.labelMedium,
                color = AppColors.TextSecondary,
                modifier = Modifier.padding(bottom = Dimensions.Spacing.small)
            )
        }
        
        // Dropdown Trigger
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Dimensions.CornerRadius.medium))
                    .background(AppColors.SurfaceHighlight)
                    .clickable { expanded = true }
                    .padding(
                        horizontal = Dimensions.Padding.standard,
                        vertical = Dimensions.Padding.medium
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = itemLabel(selectedItem),
                    style = AppTypography.bodyMedium,
                    color = AppColors.TextPrimary
                )
                
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Dropdown",
                    tint = AppColors.TextSecondary,
                    modifier = Modifier.size(Dimensions.IconSize.medium)
                )
            }
            
            // Dropdown Menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                offset = DpOffset(0.dp, Dimensions.Spacing.small),
                modifier = Modifier
                    .background(AppColors.Surface)
                    .widthIn(min = 200.dp)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = itemLabel(item),
                                    style = AppTypography.bodyMedium,
                                    color = if (item == selectedItem) AppColors.TextPrimary else AppColors.TextSecondary
                                )
                                
                                if (item == selectedItem) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = AppColors.Success,
                                        modifier = Modifier.size(Dimensions.IconSize.medium)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        },
                        modifier = Modifier.background(
                            if (item == selectedItem) AppColors.SurfaceHighlight else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}