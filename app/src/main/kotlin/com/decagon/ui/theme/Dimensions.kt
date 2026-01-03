package com.decagon.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized spacing and sizing constants.
 * Prevents hardcoded values across the app.
 */
object Dimensions {
    
    // ==================== Spacing ====================
    
    /**
     * Vertical spacing (standard 8dp grid system)
     */
    object Spacing {
        val none: Dp = 0.dp
        val extraSmall: Dp = 4.dp
        val small: Dp = 8.dp
        val medium: Dp = 12.dp
        val standard: Dp = 16.dp
        val large: Dp = 24.dp
        val extraLarge: Dp = 32.dp
        val huge: Dp = 48.dp
    }
    
    /**
     * Padding values for containers
     */
    object Padding {
        val tiny: Dp = 4.dp
        val small: Dp = 8.dp
        val medium: Dp = 12.dp
        val standard: Dp = 16.dp
        val large: Dp = 20.dp
        val extraLarge: Dp = 24.dp
    }
    
    // ==================== Component Sizes ====================
    
    /**
     * Icon sizes
     */
    object IconSize {
        val tiny: Dp = 12.dp
        val small: Dp = 16.dp
        val medium: Dp = 20.dp
        val standard: Dp = 24.dp
        val large: Dp = 32.dp
        val extraLarge: Dp = 48.dp
        val huge: Dp = 64.dp
    }
    
    /**
     * Button sizes
     */
    object Button {
        val heightSmall: Dp = 40.dp
        val heightMedium: Dp = 48.dp
        val heightLarge: Dp = 56.dp
        val heightExtraLarge: Dp = 64.dp
        
        val minWidth: Dp = 120.dp
        val iconButtonSize: Dp = 48.dp
    }
    
    /**
     * Card and container sizes
     */
    object Card {
        val minHeight: Dp = 72.dp
        val defaultElevation: Dp = 2.dp
        val pressedElevation: Dp = 8.dp
    }
    
    /**
     * Input field sizes
     */
    object Input {
        val height: Dp = 56.dp
        val heightMidCompact: Dp = 52.dp
        val heightCompact: Dp = 48.dp
    }
    
    /**
     * Avatar/Profile sizes
     */
    object Avatar {
        val tiny: Dp = 24.dp
        val small: Dp = 32.dp
        val medium: Dp = 40.dp
        val large: Dp = 48.dp
        val extraLarge: Dp = 64.dp
    }
    
    // ==================== Border & Corner Radius ====================
    
    object Border {
        val thin: Dp = 0.5.dp
        val standard: Dp = 1.dp
        val thick: Dp = 2.dp
    }
    
    object CornerRadius {
        val small: Dp = 8.dp
        val medium: Dp = 12.dp
        val standard: Dp = 16.dp
        val large: Dp = 20.dp
        val extraLarge: Dp = 24.dp
        val pill: Dp = 100.dp // For fully rounded
    }
}