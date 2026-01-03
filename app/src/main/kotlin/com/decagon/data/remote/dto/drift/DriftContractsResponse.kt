package com.decagon.data.remote.dto.drift

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Drift API response wrapper.
 * Endpoint: https://data.api.drift.trade/contracts
 * 
 * Contains both PERP and SPOT markets. Filter by product_type.
 */
@Serializable
data class DriftContractsResponse(
    @SerialName("contracts")
    val contracts: List<DriftContractDto>
)