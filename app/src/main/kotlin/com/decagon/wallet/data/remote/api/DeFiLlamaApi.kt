package com.octane.wallet.data.remote.api

import com.octane.wallet.data.remote.dto.DAppDto
import de.jensklingenberg.ktorfit.http.GET

/**
 * DeFiLlama API for protocol TVL and metrics.
 * ✅ FIXED: Using correct free public endpoint
 *
 * Base URL: https://api.llama.fi/
 * Documentation: https://defillama.com/docs/api
 * Authentication: NONE (free public API)
 */
interface DeFiLlamaApi {

    /**
     * Get all protocols with current TVL.
     * ✅ Returns ALL protocols across all chains (including Solana).
     *
     * Response: List of 2000+ protocols
     * Rate limit: None for free tier
     */
    @GET("protocols")
    suspend fun getProtocols(): List<DAppDto>

    /**
     * Get specific protocol details.
     * Note: slug must be lowercase (e.g., "aave", "uniswap")
     */
    @GET("protocol/{slug}")
    suspend fun getProtocol(
        @de.jensklingenberg.ktorfit.http.Path("slug") slug: String
    ): DAppDto
}