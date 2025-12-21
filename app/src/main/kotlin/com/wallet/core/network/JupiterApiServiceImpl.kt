package com.octane.wallet.core.network

import com.wallet.core.blockchain.JupiterApiService
import com.wallet.core.blockchain.JupiterPrice
import com.wallet.core.blockchain.JupiterQuoteResponse
import com.wallet.core.blockchain.JupiterSwapRequest
import com.wallet.core.blockchain.JupiterSwapResponse
import com.wallet.core.blockchain.JupiterToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

// ==================== RETROFIT IMPLEMENTATION ====================

class JupiterApiServiceImpl(
    private val httpClient: HttpClient
) : JupiterApiService {
    
    private val baseUrl = "https://quote-api.jup.ag/v6"
    
    override suspend fun getQuote(
        inputMint: String,
        outputMint: String,
        amount: Long,
        slippageBps: Int,
        onlyDirectRoutes: Boolean,
        asLegacyTransaction: Boolean
    ): JupiterQuoteResponse {
        return httpClient.get("$baseUrl/quote") {
            parameter("inputMint", inputMint)
            parameter("outputMint", outputMint)
            parameter("amount", amount)
            parameter("slippageBps", slippageBps)
            parameter("onlyDirectRoutes", onlyDirectRoutes)
            parameter("asLegacyTransaction", asLegacyTransaction)
        }.body()
    }
    
    override suspend fun getSwapTransaction(
        request: JupiterSwapRequest
    ): JupiterSwapResponse {
        return httpClient.post("$baseUrl/swap") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
    
    override suspend fun getTokens(): List<JupiterToken> {
        return httpClient.get("$baseUrl/tokens").body()
    }
    
    override suspend fun getPrice(ids: String): Map<String, JupiterPrice> {
        return httpClient.get("$baseUrl/price") {
            parameter("ids", ids)
        }.body()
    }
}