package com.decagon.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Centralized color palette.
 * Follows dark theme design with metallic accents.
 */
object AppColors {
    
    // ==================== Background & Surface ====================
    
    val Background = Color(0xFF000000)           // Pure black
    val Surface = Color(0xFF0A0A0A)              // Card background
    val SurfaceHighlight = Color(0xFF1C1C1E)     // Input fields, pressed states
    val SurfaceVariant = Color(0xFF2C2C2E)       // Alternate surface
    
    // ==================== Text ====================
    
    val TextPrimary = Color(0xFFFFFFFF)          // Main text
    val TextSecondary = Color(0xFF8E8E93)        // Secondary text
    val IconSecondary = Color(0xFF626267)        // Secondary Icon
    val TextTertiary = Color(0xFF48484A)         // Disabled/placeholder
    val TextDisabled = Color(0xFF3A3A3C)         // Fully disabled
    
    // ==================== Borders & Dividers ====================
    
    val BorderDefault = Color(0xFF48484A)        // Standard border
    val BorderSubtle = Color(0xFF2C2C2E)         // Subtle divider
    val BorderHighlight = Color.White            // Metallic highlight
    
    // ==================== Brand/Accent Colors ====================
    
    val Bitcoin = Color(0xFFF7931A)
    val Ethereum = Color(0xFF627EEA)
    val Solana = Color(0xFF9945FF)
    val Polygon = Color(0xFF8247E5)
    val USDC = Color(0xFF2775CA)
    val USDT = Color(0xFF26A17B)
    
    // ==================== Status/Semantic Colors ====================
    
    val Success = Color(0xFF4ECDC4)              // Positive/gain (Teal)
    val SuccessContainer = Color(0xFF1A4D4A)     // Success background
    
    val Error = Color(0xFFFF6B6B)                // Negative/loss (Red)
    val ErrorContainer = Color(0xFF4D1A1A)       // Error background
    
    val Warning = Color(0xFFF7DC6F)              // Warning (Yellow)
    val WarningContainer = Color(0xFF4D4A1A)     // Warning background
    
    val Info = Color(0xFF85C1E2)                 // Info (Blue)
    val InfoContainer = Color(0xFF1A3A4D)        // Info background
    
    val Neutral = Color(0xFF8E8E93)              // Neutral/unchanged (Gray)
    
    // ==================== Interactive States ====================
    
    val Primary = Color.White                    // Primary action
    val PrimaryPressed = Color(0xFFD1D1D6)       // Pressed state
    
    val Secondary = Surface                      // Secondary action
    val SecondaryPressed = SurfaceHighlight      // Pressed state
    
    // ==================== Overlays ====================
    
    val Overlay = Color(0x80000000)              // 50% black overlay
    val OverlayLight = Color(0x40000000)         // 25% black overlay
    val OverlayHeavy = Color(0xCC000000)         // 80% black overlay
    
    // ==================== Gradients ====================
    
    val MetallicGradientStart = Color.White
    val MetallicGradientMid = Color.Black
    val MetallicGradientEnd = Color.White
}