package com.decagon.core.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import timber.log.Timber

/**
 * Generates QR codes for wallet addresses.
 * Uses ZXing library for encoding.
 */
object DecagonQrGenerator {
    
    /**
     * Generates QR code bitmap from wallet address.
     * 
     * @param address Wallet address (Base58 for Solana, 0x... for EVM)
     * @param size QR code size in pixels (default: 512)
     * @param foregroundColor QR code color (default: black)
     * @param backgroundColor Background color (default: white)
     * @return Bitmap of QR code
     */
    fun generateQrCode(
        address: String,
        size: Int = 512,
        foregroundColor: Int = android.graphics.Color.BLACK,
        backgroundColor: Int = android.graphics.Color.WHITE
    ): Result<Bitmap> {
        Timber.d("Generating QR code for address: ${address.take(8)}...")
        
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M)
                put(EncodeHintType.MARGIN, 1) // Border size
            }
            
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(address, BarcodeFormat.QR_CODE, size, size, hints)
            
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(
                        x, 
                        y, 
                        if (bitMatrix[x, y]) foregroundColor else backgroundColor
                    )
                }
            }
            
            Timber.i("QR code generated successfully")
            Result.success(bitmap)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate QR code")
            Result.failure(e)
        }
    }
    
    /**
     * Generates Solana-specific QR with solana: URI scheme.
     * 
     * Format: solana:<address>?amount=<amount>&label=<label>
     * 
     * @param address Solana address
     * @param amount Optional amount in SOL
     * @param label Optional label/memo
     */
    fun generateSolanaQr(
        address: String,
        amount: Double? = null,
        label: String? = null,
        size: Int = 512
    ): Result<Bitmap> {
        val uri = buildString {
            append("solana:$address")
            
            val params = mutableListOf<String>()
            amount?.let { params.add("amount=$it") }
            label?.let { params.add("label=${it.replace(" ", "%20")}") }
            
            if (params.isNotEmpty()) {
                append("?${params.joinToString("&")}")
            }
        }
        
        Timber.d("Generating Solana QR with URI: $uri")
        return generateQrCode(uri, size)
    }
}