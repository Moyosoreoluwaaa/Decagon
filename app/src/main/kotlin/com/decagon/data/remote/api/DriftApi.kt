package com.decagon.data.remote.api

import com.decagon.data.remote.dto.drift.DriftContractsResponse
import de.jensklingenberg.ktorfit.http.GET

/**
 * Drift Protocol API interface.
 * 
 * Base URL: https://data.api.drift.trade/
 * Documentation: https://docs.drift.trade/api-specification
 * 
 * Drift is a decentralized perpetual futures exchange on Solana.
 * Provides real-time market data for perps trading.
 */
interface DriftApi {

    /**
     * Get all perpetual futures and spot markets.
     * 
     * Endpoint: GET /contracts
     * Returns: List of all contracts (PERP + SPOT)
     * 
     * IMPORTANT: Response includes SPOT markets.
     * Filter by `product_type == "PERP"` to get only perpetuals.
     * 
     * Example response:
     * ```json
     * {
     *   "contracts": [
     *     {
     *       "contract_index": 0,
     *       "ticker_id": "SOL-PERP",
     *       "base_currency": "SOL",
     *       "product_type": "PERP",
     *       "last_price": "238.979183",
     *       "funding_rate": "0.002327164",
     *       ...
     *     },
     *     {
     *       "ticker_id": "SOL",
     *       "product_type": "SPOT",
     *       ...
     *     }
     *   ]
     * }
     * ```
     * 
     * @return DriftContractsResponse containing all contracts
     * @throws Exception if API call fails
     */
    @GET("contracts")
    suspend fun getContracts(): DriftContractsResponse
}