package com.decagon.data.service

import com.decagon.data.remote.api.DiscoverApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Resolves token logos dynamically from CoinGecko.
 * Falls back to hardcoded URLs for common tokens.
 */
class TokenLogoResolver(
    private val coinGeckoApi: DiscoverApi
) {
    
    // Cache to avoid repeated API calls
    private val logoCache = mutableMapOf<String, String?>()
    
    // Hardcoded fallbacks for popular tokens (fast path)
    // data/services/TokenLogoResolver.kt

    private val hardcodedLogos = mapOf(
        // Layer 1s
        "SOL" to "https://assets.coingecko.com/coins/images/4128/large/solana.png",
        "BTC" to "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
        "ETH" to "https://assets.coingecko.com/coins/images/279/large/ethereum.png",

        // Stablecoins
        "USDC" to "https://assets.coingecko.com/coins/images/6319/large/USD_Coin_icon.png",
        "USDT" to "https://assets.coingecko.com/coins/images/325/large/Tether.png",
        "DAI" to "https://assets.coingecko.com/coins/images/9956/large/Badge_Dai.png",
        "PYUSD" to "https://assets.coingecko.com/coins/images/31212/large/PYUSD_Logo_%282%29.png",

        // Solana Ecosystem
        "BONK" to "https://assets.coingecko.com/coins/images/28600/large/bonk.jpg",
        "JUP" to "https://assets.coingecko.com/coins/images/10351/large/logo512.png",
        "WIF" to "https://assets.coingecko.com/coins/images/33566/large/dogwifhat.jpg",
        "PYTH" to "https://assets.coingecko.com/coins/images/31924/large/pyth.png",
        "JTO" to "https://assets.coingecko.com/coins/images/33228/large/jito.png",
        "RNDR" to "https://assets.coingecko.com/coins/images/11636/large/rndr.png",
        "HNT" to "https://assets.coingecko.com/coins/images/10103/large/helium.png",
        "MNGO" to "https://assets.coingecko.com/coins/images/17120/large/mngo.png",
        "RAY" to "https://assets.coingecko.com/coins/images/13928/large/PSigc4ie_400x400.jpg",
        "SRM" to "https://assets.coingecko.com/coins/images/11970/large/serum-logo.png",
        "ORCA" to "https://assets.coingecko.com/coins/images/17547/large/Orca_Logo.png",
        "FIDA" to "https://assets.coingecko.com/coins/images/14570/large/fida.png",
        "STEP" to "https://assets.coingecko.com/coins/images/14988/large/step.png",
        "COPE" to "https://assets.coingecko.com/coins/images/14565/large/cope.png",
        "MEDIA" to "https://assets.coingecko.com/coins/images/14565/large/media.png",
        "SNY" to "https://assets.coingecko.com/coins/images/13866/large/sny.png",
        "PORT" to "https://assets.coingecko.com/coins/images/15127/large/port.png",
        "ATLAS" to "https://assets.coingecko.com/coins/images/17659/large/atlas_logo.png",
        "POLIS" to "https://assets.coingecko.com/coins/images/17644/large/polis.png",
        "GRAPE" to "https://assets.coingecko.com/coins/images/17465/large/grape.png",
        "SLND" to "https://assets.coingecko.com/coins/images/17879/large/slnd.png",
        "TULIP" to "https://assets.coingecko.com/coins/images/14565/large/tulip.png",
        "SUNNY" to "https://assets.coingecko.com/coins/images/17223/large/sunny.png",
        "SAMO" to "https://assets.coingecko.com/coins/images/15051/large/IXeEj2Z.png",
        "DUST" to "https://assets.coingecko.com/coins/images/25479/large/dust.png",
        "FORGE" to "https://assets.coingecko.com/coins/images/24847/large/forge.png",
        "RENDER" to "https://assets.coingecko.com/coins/images/11636/large/rndr.png",
        "W" to "https://assets.coingecko.com/coins/images/35087/large/womrhole_logo_full_color_rgb_2000px_72ppi_fb766ac85a.png",
        "MOBILE" to "https://assets.coingecko.com/coins/images/31087/large/MOBILE_LOGO.png",
        "IOT" to "https://assets.coingecko.com/coins/images/31086/large/IOT_logo.png",
        "MEW" to "https://assets.coingecko.com/coins/images/36890/large/mew.jpg",
        "POPCAT" to "https://assets.coingecko.com/coins/images/37207/large/POPCAT.png",

        // Major L1s/L2s
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
        "FTM" to "https://assets.coingecko.com/coins/images/4001/large/Fantom_round.png",
        "NEAR" to "https://assets.coingecko.com/coins/images/10365/large/near.jpg",
        "ALGO" to "https://assets.coingecko.com/coins/images/4380/large/download.png",

        // DeFi Tokens
        "UNI" to "https://assets.coingecko.com/coins/images/12504/large/uni.jpg",
        "LINK" to "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png",
        "AAVE" to "https://assets.coingecko.com/coins/images/12645/large/aave.png",
        "CRV" to "https://assets.coingecko.com/coins/images/12124/large/Curve.png",
        "MKR" to "https://assets.coingecko.com/coins/images/1364/large/Mark_Maker.png",
        "SNX" to "https://assets.coingecko.com/coins/images/3406/large/SNX.png",
        "COMP" to "https://assets.coingecko.com/coins/images/10775/large/COMP.png",
        "SUSHI" to "https://assets.coingecko.com/coins/images/12271/large/512x512_Logo_no_chop.png",
        "1INCH" to "https://assets.coingecko.com/coins/images/13469/large/1inch-token.png",
        "BAL" to "https://assets.coingecko.com/coins/images/11683/large/Balancer.png",
        "YFI" to "https://assets.coingecko.com/coins/images/11849/large/yearn.jpg",

        // Memecoins
        "DOGE" to "https://assets.coingecko.com/coins/images/5/large/dogecoin.png",
        "SHIB" to "https://assets.coingecko.com/coins/images/11939/large/shiba.png",
        "PEPE" to "https://assets.coingecko.com/coins/images/29850/large/pepe-token.jpeg",
        "FLOKI" to "https://assets.coingecko.com/coins/images/16746/large/FLOKI.png",

        // Other Major Tokens
        "BNB" to "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png",
        "LTC" to "https://assets.coingecko.com/coins/images/2/large/litecoin.png",
        "DOT" to "https://assets.coingecko.com/coins/images/12171/large/polkadot.png",
        "TRX" to "https://assets.coingecko.com/coins/images/1094/large/tron-logo.png",
        "XLM" to "https://assets.coingecko.com/coins/images/100/large/Stellar_symbol_black_RGB.png",
        "XRP" to "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png",
        "ADA" to "https://assets.coingecko.com/coins/images/975/large/cardano.png"
    )
    
    /**
     * Resolve logo URL for a token symbol.
     * 
     * Priority:
     * 1. Check cache
     * 2. Check hardcoded map
     * 3. Search CoinGecko API
     * 4. Return null (will show fallback letter)
     */
    suspend fun resolveLogoUrl(symbol: String): String? = withContext(Dispatchers.IO) {
        val normalizedSymbol = symbol.uppercase().trim()
        
        // 1. Check cache
        if (logoCache.containsKey(normalizedSymbol)) {
            Timber.d("📸 Logo cache HIT: $normalizedSymbol -> ${logoCache[normalizedSymbol]}")
            return@withContext logoCache[normalizedSymbol]
        }
        
        // 2. Check hardcoded map (fast path)
        hardcodedLogos[normalizedSymbol]?.let { url ->
            Timber.d("📸 Logo hardcoded: $normalizedSymbol -> $url")
            logoCache[normalizedSymbol] = url
            return@withContext url
        }
        
        // 3. Search CoinGecko (slow path)
        try {
            Timber.d("🔍 Searching CoinGecko for logo: $normalizedSymbol")
            
            val searchResponse = coinGeckoApi.searchTokens(normalizedSymbol)
            
            // Find exact match (case-insensitive)
            val match = searchResponse.coins.firstOrNull { 
                it.symbol.equals(normalizedSymbol, ignoreCase = true) 
            }
            
            val logoUrl = match?.thumb
            
            if (logoUrl != null) {
                Timber.i("✅ CoinGecko logo found: $normalizedSymbol -> $logoUrl")
            } else {
                Timber.w("⚠️ No logo found for: $normalizedSymbol")
            }
            
            // Cache result (even if null to avoid repeated API calls)
            logoCache[normalizedSymbol] = logoUrl
            
            return@withContext logoUrl
            
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to fetch logo for $normalizedSymbol")
            
            // Cache null to avoid repeated failures
            logoCache[normalizedSymbol] = null
            return@withContext null
        }
    }
    
    /**
     * Resolve logos for multiple symbols (batch operation).
     */
    suspend fun resolveLogosForSymbols(symbols: List<String>): Map<String, String?> {
        return symbols.associateWith { symbol ->
            resolveLogoUrl(symbol)
        }
    }
    
    /**
     * Clear cache (call on data refresh).
     */
    fun clearCache() {
        logoCache.clear()
        Timber.d("🗑️ Logo cache cleared")
    }
}