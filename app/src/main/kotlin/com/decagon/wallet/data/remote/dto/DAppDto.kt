package com.octane.wallet.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DeFiLlama API response for /protocols endpoint.
 * ✅ FIXED: Matches actual API response structure
 *
 * Endpoint: https://api.llama.fi/protocols
 * Documentation: https://defillama.com/docs/api
 */
@Serializable
data class DAppDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("address")
    val address: String? = null,

    @SerialName("symbol")
    val symbol: String? = null,

    @SerialName("url")
    val url: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("chain")
    val chain: String? = "Solana",

    @SerialName("logo")
    val logo: String? = null,

    @SerialName("audits")
    val audits: String? = null,

    @SerialName("audit_note")
    val audit_note: String? = null,

    @SerialName("gecko_id")
    val gecko_id: String? = null,

    @SerialName("cmcId")
    val cmcId: String? = null,

    @SerialName("category")
    val category: String,

    @SerialName("chains")
    val chains: List<String> = emptyList(),

    @SerialName("module")
    val module: String? = null,

    @SerialName("twitter")
    val twitter: String? = null,

    @SerialName("forkedFrom")
    val forkedFrom: List<String>? = null,

    @SerialName("oracles")
    val oracles: List<String>? = null,

    @SerialName("listedAt")
    val listedAt: Long? = null,

    @SerialName("slug")
    val slug: String,

    // ✅ FIXED: Current TVL is a simple number, not nested object
    @SerialName("tvl")
    val tvl: Double? = null,

    @SerialName("chainTvls")
    val chainTvls: Map<String, Double>? = null,

    @SerialName("change_1h")
    val change_1h: Double? = null,

    @SerialName("change_1d")
    val change_1d: Double? = null,

    @SerialName("change_7d")
    val change_7d: Double? = null,

    @SerialName("staking")
    val staking: Double? = null,

    @SerialName("fdv")
    val fdv: Double? = null,

    @SerialName("mcap")
    val mcap: Double? = null
)