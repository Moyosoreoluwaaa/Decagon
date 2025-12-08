package com.decagon.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import timber.log.Timber

/**
 * Utility for opening Solana blockchain explorer links.
 */
object DecagonExplorerUtil {
    
    private const val SOLSCAN_BASE = "https://solscan.io"
    
    /**
     * Opens transaction in Solscan explorer.
     *
     * @param context Android context
     * @param signature Transaction signature
     */
    fun openTransaction(context: Context, signature: String) {
        try {
            val url = "$SOLSCAN_BASE/tx/$signature"
            Timber.d("Opening transaction in explorer: $url")
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open transaction explorer")
        }
    }
    
    /**
     * Opens account/address in Solscan explorer.
     *
     * @param context Android context
     * @param address Solana address
     */
    fun openAddress(context: Context, address: String) {
        try {
            val url = "$SOLSCAN_BASE/account/$address"
            Timber.d("Opening address in explorer: $url")
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open address explorer")
        }
    }
    
    /**
     * Opens token account in Solscan explorer.
     *
     * @param context Android context
     * @param tokenAddress Token mint address
     */
    fun openToken(context: Context, tokenAddress: String) {
        try {
            val url = "$SOLSCAN_BASE/token/$tokenAddress"
            Timber.d("Opening token in explorer: $url")
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open token explorer")
        }
    }
    
    /**
     * Gets transaction explorer URL (for copying/sharing).
     *
     * @param signature Transaction signature
     * @return Full URL string
     */
    fun getTransactionUrl(signature: String): String {
        return "$SOLSCAN_BASE/tx/$signature"
    }
    
    /**
     * Gets address explorer URL (for copying/sharing).
     *
     * @param address Solana address
     * @return Full URL string
     */
    fun getAddressUrl(address: String): String {
        return "$SOLSCAN_BASE/account/$address"
    }
}