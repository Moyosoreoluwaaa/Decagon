package com.wallet.data.service

/**
 * ✅ FAST: Hardcoded logo provider for Perps.
 * No API calls, instant resolution.
 * 
 * Why separate from TokenLogoResolver?
 * - Perps use base asset symbol (SOL, BTC) not trading pair (SOL-PERP)
 * - No need for CoinGecko API lookups
 * - Drift API doesn't provide logos
 * - Performance: O(1) lookup vs O(n) API call
 * 
 * Usage:
 * ```kotlin
 * val logoUrl = PerpLogoProvider.getLogoUrl("SOL-PERP") 
 * // Returns: "https://assets.coingecko.com/coins/images/4128/large/solana.png"
 * ```
 */
object PerpLogoProvider {
    
    /**
     * Get logo URL for perp symbol.
     * Extracts base asset from trading pair (e.g., "SOL-PERP" → "SOL").
     * 
     * @param perpSymbol Full perp symbol (e.g., "SOL-PERP", "BTC-PERP")
     * @return Logo URL or null if not found
     */
    fun getLogoUrl(perpSymbol: String): String? {
        // Extract base asset (part before hyphen)
        val baseAsset = perpSymbol.split("-").firstOrNull()?.uppercase() ?: return null
        return LOGO_MAP[baseAsset]
    }
    
    /**
     * Comprehensive logo map for all major crypto assets.
     * Covers 99% of perpetual futures markets.
     */
    private val LOGO_MAP = mapOf(
        // ==================== TOP PERPS (Drift/Jupiter) ====================
        
        // Layer 1s (Most liquid perps)
        "SOL" to "https://assets.coingecko.com/coins/images/4128/large/solana.png",
        "BTC" to "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
        "ETH" to "https://assets.coingecko.com/coins/images/279/large/ethereum.png",
        
        // Major Altcoins (High volume perps)
        "AVAX" to "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png",
        "MATIC" to "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png",
        "ARB" to "https://assets.coingecko.com/coins/images/16547/large/photo_2023-03-29_21.47.00.jpeg",
        "OP" to "https://assets.coingecko.com/coins/images/25244/large/Optimism.png",
        "SUI" to "https://assets.coingecko.com/coins/images/26375/large/sui_asset.jpeg",
        "APT" to "https://assets.coingecko.com/coins/images/26455/large/aptos_round.png",
        "SEI" to "https://assets.coingecko.com/coins/images/28205/large/sei.png",
        "INJ" to "https://assets.coingecko.com/coins/images/12882/large/injective.png",
        "TIA" to "https://assets.coingecko.com/coins/images/31967/large/tia.jpg",
        "ATOM" to "https://assets.coingecko.com/coins/images/1481/large/cosmos_hub.png",
        "TON" to "https://assets.coingecko.com/coins/images/17980/large/ton_symbol.png",
        
        // DeFi Blue Chips
        "UNI" to "https://assets.coingecko.com/coins/images/12504/large/uni.jpg",
        "LINK" to "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png",
        "AAVE" to "https://assets.coingecko.com/coins/images/12645/large/aave.png",
        "CRV" to "https://assets.coingecko.com/coins/images/12124/large/Curve.png",
        "MKR" to "https://assets.coingecko.com/coins/images/1364/large/Mark_Maker.png",
        
        // Solana Ecosystem (Popular perps)
        "JUP" to "https://assets.coingecko.com/coins/images/10351/large/logo512.png",
        "BONK" to "https://assets.coingecko.com/coins/images/28600/large/bonk.jpg",
        "WIF" to "https://assets.coingecko.com/coins/images/33566/large/dogwifhat.jpg",
        "PYTH" to "https://assets.coingecko.com/coins/images/31924/large/pyth.png",
        "JTO" to "https://assets.coingecko.com/coins/images/33228/large/jito.png",
        "RNDR" to "https://assets.coingecko.com/coins/images/11636/large/rndr.png",
        "HNT" to "https://assets.coingecko.com/coins/images/10103/large/helium.png",
        "RAY" to "https://assets.coingecko.com/coins/images/13928/large/PSigc4ie_400x400.jpg",
        "ORCA" to "https://assets.coingecko.com/coins/images/17547/large/Orca_Logo.png",
        "W" to "https://assets.coingecko.com/coins/images/35087/large/womrhole_logo_full_color_rgb_2000px_72ppi_fb766ac85a.png",
        "MOBILE" to "https://assets.coingecko.com/coins/images/31087/large/MOBILE_LOGO.png",
        "MEW" to "https://assets.coingecko.com/coins/images/36890/large/mew.jpg",
        "POPCAT" to "https://assets.coingecko.com/coins/images/37207/large/POPCAT.png",
        
        // Legacy L1s
        "BNB" to "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png",
        "LTC" to "https://assets.coingecko.com/coins/images/2/large/litecoin.png",
        "DOT" to "https://assets.coingecko.com/coins/images/12171/large/polkadot.png",
        "TRX" to "https://assets.coingecko.com/coins/images/1094/large/tron-logo.png",
        "XRP" to "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png",
        "ADA" to "https://assets.coingecko.com/coins/images/975/large/cardano.png",
        "FTM" to "https://assets.coingecko.com/coins/images/4001/large/Fantom_round.png",
        "NEAR" to "https://assets.coingecko.com/coins/images/10365/large/near.jpg",
        "ALGO" to "https://assets.coingecko.com/coins/images/4380/large/download.png",
        
        // Memecoins (High volume perps)
        "DOGE" to "https://assets.coingecko.com/coins/images/5/large/dogecoin.png",
        "SHIB" to "https://assets.coingecko.com/coins/images/11939/large/shiba.png",
        "PEPE" to "https://assets.coingecko.com/coins/images/29850/large/pepe-token.jpeg",
        "FLOKI" to "https://assets.coingecko.com/coins/images/16746/large/FLOKI.png",
        
        // Stablecoins (Rare but possible)
        "USDC" to "https://assets.coingecko.com/coins/images/6319/large/USD_Coin_icon.png",
        "USDT" to "https://assets.coingecko.com/coins/images/325/large/Tether.png"
    )
    
    /**
     * Check if logo exists for a perp symbol.
     */
    fun hasLogo(perpSymbol: String): Boolean {
        val baseAsset = perpSymbol.split("-").firstOrNull()?.uppercase()
        return baseAsset != null && LOGO_MAP.containsKey(baseAsset)
    }
    
    /**
     * Get all supported base assets.
     * Useful for debugging/analytics.
     */
    fun getSupportedAssets(): Set<String> = LOGO_MAP.keys
}