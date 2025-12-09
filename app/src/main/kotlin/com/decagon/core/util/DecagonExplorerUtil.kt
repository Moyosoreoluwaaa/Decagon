package com.decagon.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.decagon.core.chains.ChainRegistry
import com.decagon.core.chains.ChainType
import timber.log.Timber

object DecagonExplorerUtil {

    /**
     * Opens transaction in chain-specific explorer.
     */
    fun openTransaction(context: Context, chainId: String, txHash: String) {
        try {
            val config = ChainRegistry.getChain(chainId)
            val url = when (ChainType.fromId(chainId)) {
                ChainType.Solana -> "${config.explorerUrl}/tx/$txHash"
                ChainType.Ethereum -> "${config.explorerUrl}/tx/$txHash"
                ChainType.Polygon -> "${config.explorerUrl}/tx/$txHash"
            }

            Timber.d("Opening transaction: $url")
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Timber.e(e, "Failed to open transaction explorer")
        }
    }

    /**
     * Opens address in chain-specific explorer.
     */
    fun openAddress(context: Context, chainId: String, address: String) {
        try {
            val config = ChainRegistry.getChain(chainId)
            val url = when (ChainType.fromId(chainId)) {
                ChainType.Solana -> "${config.explorerUrl}/account/$address"
                ChainType.Ethereum -> "${config.explorerUrl}/address/$address"
                ChainType.Polygon -> "${config.explorerUrl}/address/$address"
            }

            Timber.d("Opening address: $url")
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            Timber.e(e, "Failed to open address explorer")
        }
    }

    /**
     * Gets transaction URL for sharing/copying.
     */
    fun getTransactionUrl(chainId: String, txHash: String): String {
        val config = ChainRegistry.getChain(chainId)
        return when (ChainType.fromId(chainId)) {
            ChainType.Solana -> "${config.explorerUrl}/tx/$txHash"
            ChainType.Ethereum -> "${config.explorerUrl}/tx/$txHash"
            ChainType.Polygon -> "${config.explorerUrl}/tx/$txHash"
        }
    }

    /**
     * Gets address URL for sharing/copying.
     */
    fun getAddressUrl(chainId: String, address: String): String {
        val config = ChainRegistry.getChain(chainId)
        return when (ChainType.fromId(chainId)) {
            ChainType.Solana -> "${config.explorerUrl}/account/$address"
            ChainType.Ethereum -> "${config.explorerUrl}/address/$address"
            ChainType.Polygon -> "${config.explorerUrl}/address/$address"
        }
    }
}