package com.decagon.data.remote.api

import com.decagon.data.remote.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
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

    // ✅ Lenient JSON parser for error handling
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * GET /ultra/v1/order
     * Retrieves swap order with unsigned transaction.
     *
     * ✅ FIXED: Handles empty transaction and insufficient liquidity specific cases
     */
    suspend fun getOrder(request: JupiterOrderRequest): Result<JupiterOrderResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Fetching swap order: ${request.inputMint.take(8)}... -> ${request.outputMint.take(8)}..., amount: ${request.amount}")

                val response: HttpResponse = httpClient.get("$ultraV1/order") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                    parameter("inputMint", request.inputMint)
                    parameter("outputMint", request.outputMint)
                    parameter("amount", request.amount)
                    parameter("taker", request.taker)
                    request.slippageBps?.let { parameter("slippageBps", it) }
                }

                // ✅ Check HTTP status before deserializing
                if (!response.status.isSuccess()) {
                    val errorBody = response.bodyAsText()
                    Timber.e("Jupiter API error (${response.status.value}): $errorBody")

                    // Parse common error messages from HTTP error body
                    val errorMessage = when {
                        errorBody.contains("No routes found", ignoreCase = true) ->
                            "No trading route available for this token pair"
                        errorBody.contains("Insufficient liquidity", ignoreCase = true) ->
                            "Insufficient liquidity for this swap"
                        errorBody.contains("Invalid", ignoreCase = true) ->
                            "Invalid swap parameters"
                        else ->
                            "Jupiter API error: ${response.status.value}"
                    }

                    return@withContext Result.failure(
                        JupiterApiException(errorMessage, response.status.value)
                    )
                }

                // ✅ Safely deserialize response
                val orderResponse = try {
                    response.body<JupiterOrderResponse>()
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse Jupiter order response")
                    val bodyText = response.bodyAsText()
                    Timber.e("Response body: $bodyText")

                    return@withContext Result.failure(
                        JupiterApiException(
                            "Unable to process swap quote",
                            response.status.value
                        )
                    )
                }

                // ✅ CRITICAL FIX: Validate response fields with specific error messages for ViewModel mapping
                when {
                    orderResponse.transaction.isBlank() -> {
                        Timber.w("Jupiter returned empty transaction for ${request.inputMint.take(8)}... → ${request.outputMint.take(8)}...")
                        return@withContext Result.failure(
                            JupiterApiException(
                                "No trading route available for this token pair",
                                0
                            )
                        )
                    }

                    orderResponse.outAmount.toDoubleOrNull()?.let { it <= 0 } == true -> {
                        Timber.w("Jupiter returned invalid output amount: ${orderResponse.outAmount}")
                        return@withContext Result.failure(
                            JupiterApiException(
                                "Insufficient liquidity for this swap",
                                0
                            )
                        )
                    }
                }

                Timber.i("Swap order received: ${orderResponse.mode}, slippage: ${orderResponse.slippageBps} bps")
                Result.success(orderResponse)

            } catch (e: JupiterApiException) {
                // Already handled above, just pass through
                Result.failure(e)
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch swap order")
                Result.failure(
                    JupiterApiException(
                        "Network error: ${e.message ?: "Unable to connect to Jupiter"}",
                        0
                    )
                )
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

                val response: HttpResponse = httpClient.post("$ultraV1/execute") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                if (!response.status.isSuccess()) {
                    val errorBody = response.bodyAsText()
                    Timber.e("Execute swap error (${response.status.value}): $errorBody")

                    return@withContext Result.failure(
                        JupiterApiException(
                            "Failed to execute swap: ${response.status.value}",
                            response.status.value
                        )
                    )
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
     */
    suspend fun getBalances(publicKey: String): Result<JupiterBalancesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Fetching token balances for: ${publicKey.take(8)}...")

                val response: HttpResponse = httpClient.get("$ultraV1/balances/$publicKey") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                }

                if (!response.status.isSuccess()) {
                    val errorBody = response.bodyAsText()
                    Timber.e("Balances API error: $errorBody")
                    return@withContext Result.failure(
                        Exception("Failed to fetch balances: ${response.status.value}")
                    )
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
     */
    suspend fun searchTokens(query: String, limit: Int = 20): Result<JupiterSearchResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Searching tokens: $query")

                val response: HttpResponse = httpClient.get("$ultraV1/search") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                    parameter("query", query)
                    parameter("limit", limit)
                }

                if (!response.status.isSuccess()) {
                    Timber.w("Search API returned ${response.status.value}")
                    // Return empty results instead of failing
                    return@withContext Result.success(JupiterSearchResponse(emptyList()))
                }

                val searchResponse = response.body<JupiterSearchResponse>()
                Timber.i("Found ${searchResponse.results.size} tokens")

                Result.success(searchResponse)

            } catch (e: Exception) {
                Timber.e(e, "Token search failed")
                // Return empty results instead of failing
                Result.success(JupiterSearchResponse(emptyList()))
            }
        }
    }

    /**
     * GET /ultra/v1/shield?mints={mint1,mint2,...}
     */
    suspend fun getShield(mints: List<String>): Result<JupiterShieldResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Timber.d("Checking token security for ${mints.size} mints")

                val response: HttpResponse = httpClient.get("$ultraV1/shield") {
                    headers {
                        append("x-api-key", apiKey)
                    }
                    parameter("mints", mints.joinToString(","))
                }

                if (!response.status.isSuccess()) {
                    Timber.w("Shield API returned ${response.status.value}")
                    // Return empty warnings instead of failing
                    return@withContext Result.success(JupiterShieldResponse(emptyMap()))
                }

                val shieldResponse = response.body<JupiterShieldResponse>()
                Timber.i("Shield check complete: ${shieldResponse.warnings.size} tokens have warnings")

                Result.success(shieldResponse)

            } catch (e: Exception) {
                Timber.e(e, "Shield API failed")
                // Return empty warnings instead of failing
                Result.success(JupiterShieldResponse(emptyMap()))
            }
        }
    }
}

/**
 * Custom exception for Jupiter API errors.
 * Provides user-friendly error messages.
 */
class JupiterApiException(
    message: String,
    val statusCode: Int
) : Exception(message)