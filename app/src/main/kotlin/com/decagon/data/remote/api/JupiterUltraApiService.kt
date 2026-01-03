package com.decagon.data.remote.api

import com.decagon.data.remote.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Jupiter Ultra API v1 Service
 *
 * Migration Notice: lite-api.jup.ag will be deprecated Dec 31, 2025.
 * Switch to api.jup.ag (requires API key).
 *
 * API Key: 73ca3287-e794-4b2b-8f1d-02bc8bf60e8f
 */
class JupiterUltraApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://lite-api.jup.ag",
    private val apiKey: String = "73ca3287-e794-4b2b-8f1d-02bc8bf60e8f"
) {

    init {
        Timber.d("JupiterUltraApiService initialized with baseUrl: $baseUrl")
    }

    private val ultraV1 = "$baseUrl/ultra/v1"

    /**
     * GET /ultra/v1/order
     * Retrieves swap order with unsigned transaction.
     */
    suspend fun getOrder(request: JupiterOrderRequest): Result<JupiterOrderResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Fetching swap order: ${request.inputMint} -> ${request.outputMint}, amount: ${request.amount}")

                val response = httpClient.get("$ultraV1/order") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                    parameter("inputMint", request.inputMint)
                    parameter("outputMint", request.outputMint)
                    parameter("amount", request.amount)
                    parameter("taker", request.taker)
                    request.slippageBps?.let { parameter("slippageBps", it) }
                }

                val orderResponse = response.body<JupiterOrderResponse>()
                Timber.i("Swap order received: ${orderResponse.mode}, slippage: ${orderResponse.slippageBps} bps")

                Result.success(orderResponse)

            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch swap order")
                Result.failure(e)
            }
        }
    }

    /**
     * POST /ultra/v1/execute
     * Executes signed swap transaction through Jupiter.
     */
    suspend fun executeOrder(request: JupiterExecuteRequest): Result<JupiterExecuteResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Executing swap with requestId: ${request.requestId}")

                val response = httpClient.post("$ultraV1/execute") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                val executeResponse = response.body<JupiterExecuteResponse>()
                Timber.i("Swap execution status: ${executeResponse.status}")

                Result.success(executeResponse)

            } catch (e: Exception) {
                Timber.e(e, "Failed to execute swap")
                Result.failure(e)
            }
        }
    }

    /**
     * GET /ultra/v1/balances/{publicKey}
     * CRITICAL: Endpoint is "balances", NOT "holdings"
     */
    suspend fun getBalances(publicKey: String): Result<JupiterBalancesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Fetching token balances for: ${publicKey.take(8)}...")

                val response = httpClient.get("$ultraV1/balances/$publicKey") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                }

                val balancesResponse = response.body<JupiterBalancesResponse>()
                Timber.i("Fetched ${balancesResponse.balances} token balances")

                Result.success(balancesResponse)

            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch balances")
                Result.failure(e)
            }
        }
    }

    /**
     * GET /ultra/v1/search?query={query}
     * Searches tokens by symbol, name, or mint address.
     */
    suspend fun searchTokens(query: String, limit: Int = 20): Result<JupiterSearchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Searching tokens: $query")

                val response = httpClient.get("$ultraV1/search") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                    parameter("query", query)
                    parameter("limit", limit)
                }

                val searchResponse = response.body<JupiterSearchResponse>()
                Timber.i("Found ${searchResponse.results.size} tokens")

                Result.success(searchResponse)

            } catch (e: Exception) {
                Timber.e(e, "Token search failed")
                Result.failure(e)
            }
        }
    }

    /**
     * GET /ultra/v1/shield?mints={mint1,mint2,...}
     * Gets security warnings for tokens.
     */
    suspend fun getShield(mints: List<String>): Result<JupiterShieldResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Checking token security for ${mints.size} mints")

                val response = httpClient.get("$ultraV1/shield") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                    parameter("mints", mints.joinToString(","))
                }

                val shieldResponse = response.body<JupiterShieldResponse>()
                Timber.i("Shield check complete: ${shieldResponse.warnings.size} tokens have warnings")

                Result.success(shieldResponse)

            } catch (e: Exception) {
                Timber.e(e, "Shield API failed")
                Result.failure(e)
            }
        }
    }
}