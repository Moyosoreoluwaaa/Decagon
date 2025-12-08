package com.decagon.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put
import timber.log.Timber
import java.util.UUID

class SolanaRpcClient(
    private val httpClient: HttpClient,
    private val rpcUrl: String = "https://api.devnet.solana.com"
) {

    init {
        Timber.d("SolanaRpcClient initialized with URL: $rpcUrl")
    }

    suspend fun getBalance(address: String): Result<Long> {
        Timber.d("Fetching balance for address: ${address.take(4)}...${address.takeLast(4)}")
        return try {
            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", UUID.randomUUID().toString())
                    put("method", "getBalance")
                    put("params", buildJsonArray {
                        add(address)
                    })
                })
            }

            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val result = json["result"]?.jsonObject
            val lamports = result?.get("value")?.jsonPrimitive?.long ?: 0L

            Timber.i("Balance fetched: $lamports lamports")
            Result.success(lamports)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch balance")
            Result.failure(e)
        }
    }

    /**
     * Fetches the latest valid blockhash from the network.
     * This is required for transaction construction.
     */
    suspend fun getLatestBlockhash(): Result<String> {
        Timber.d("Fetching latest blockhash")
        return try {
            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", UUID.randomUUID().toString())
                    put("method", "getLatestBlockhash")
                })
            }

            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject

            // Navigate the JSON path: result.value.blockhash
            val blockhash = json["result"]
                ?.jsonObject
                ?.get("value")
                ?.jsonObject
                ?.get("blockhash")
                ?.jsonPrimitive
                ?.content

            if (blockhash.isNullOrBlank()) {
                throw IllegalStateException("Failed to parse blockhash from RPC response.")
            }

            Timber.i("Latest blockhash fetched: ${blockhash.take(8)}...")
            Result.success(blockhash)

        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch latest blockhash")
            Result.failure(e)
        }
    }

    suspend fun sendTransaction(serializedTx: ByteArray): Result<String> {
        Timber.d("Sending transaction")
        return try {
            val base64Tx = android.util.Base64.encodeToString(
                serializedTx,
                android.util.Base64.NO_WRAP
            )

            val requestBody = buildJsonObject {
                put("jsonrpc", "2.0")
                put("id", UUID.randomUUID().toString())
                put("method", "sendTransaction")
                put("params", buildJsonArray {
                    add(base64Tx)
                    // ✅ ADD THIS ENCODING CONFIG
                    add(buildJsonObject {
                        put("encoding", "base64")
                        put("skipPreflight", false)
                        put("preflightCommitment", "confirmed")
                    })
                })
            }

            Timber.d("Request body: $requestBody")

            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            val rawResponse = response.bodyAsText()
            Timber.d("RPC Response: $rawResponse")

            val json = Json.parseToJsonElement(rawResponse).jsonObject

            val error = json["error"]?.jsonObject
            if (error != null) {
                val errorMsg = error["message"]?.jsonPrimitive?.content ?: "Unknown RPC error"
                Timber.e("RPC Error: $errorMsg")
                throw IllegalStateException("RPC Error: $errorMsg")
            }

            val signature = json["result"]?.jsonPrimitive?.content
                ?: throw IllegalStateException("No signature in response")

            Timber.i("Transaction signature: ${signature.take(8)}...")
            Result.success(signature)

        } catch (e: Exception) {
            Timber.e(e, "Transaction sending failed")
            Result.failure(e)
        }
    }

    suspend fun simulateTransaction(serializedTx: ByteArray): Result<SimulationResult> {
        Timber.d("Simulating transaction")
        return try {
            val base64Tx = android.util.Base64.encodeToString(
                serializedTx,
                android.util.Base64.NO_WRAP
            )

            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", UUID.randomUUID().toString())
                    put("method", "simulateTransaction")
                    put("params", buildJsonArray {
                        add(base64Tx)
                        buildJsonObject {
                            put("encoding", "base64") // ✅ Add this
                            put("commitment", "confirmed")
                        }
                    })
                })
            }

            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val result = json["result"]?.jsonObject?.get("value")?.jsonObject
            val error = result?.get("err")

            val simulationResult = SimulationResult(
                willSucceed = error == null || error is JsonNull,
                errorMessage = error?.toString()
            )

            Timber.i("Simulation result: ${if (simulationResult.willSucceed) "SUCCESS" else "FAIL"}")
            Result.success(simulationResult)
        } catch (e: Exception) {
            Timber.e(e, "Simulation failed")
            Result.failure(e)
        }
    }

    /**
     * Gets transaction status from blockchain.
     *
     * @param signature Transaction signature
     * @return Status: "pending", "confirmed", "finalized", or "failed"
     */
    suspend fun getTransactionStatus(signature: String): Result<String> {
        Timber.d("Fetching transaction status for signature: ${signature.take(8)}...")
        return withContext(Dispatchers.IO) {
            try {
                // FIX: Replaced the unresolved 'buildJsonRpcRequest' with direct 'buildJsonObject'
                val request = buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", UUID.randomUUID().toString())
                    put("method", "getSignatureStatuses")
                    put("params", buildJsonArray {
                        // The 'getSignatureStatuses' method expects a single array containing the signatures
                        add(buildJsonArray {
                            add(signature)
                        })
                    })
                }

                val response = httpClient.post(rpcUrl) {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }

                if (!response.status.isSuccess()) {
                    Timber.e("RPC error: ${response.status}")
                    return@withContext Result.failure(
                        IOException("RPC error: ${response.status}")
                    )
                }

                val body = response.bodyAsText()
                val json = Json.parseToJsonElement(body).jsonObject

                // Check for RPC error
                json["error"]?.let { error ->
                    val errorMsg = error.jsonObject["message"]?.jsonPrimitive?.content
                        ?: "Unknown RPC error"
                    Timber.e("RPC Error in status check: $errorMsg")
                    return@withContext Result.failure(IOException(errorMsg))
                }

                // Parse result
                val result = json["result"]?.jsonObject
                    ?: return@withContext Result.failure(
                        IOException("Missing result in response")
                    )

                val statusArray = result["value"]?.jsonArray
                    ?: return@withContext Result.failure(
                        IOException("Missing value array")
                    )

                // Get first status (we only queried one signature)
                val statusObj = statusArray.firstOrNull()?.jsonObject

                if (statusObj == null) {
                    // Transaction not found yet (still pending)
                    Timber.d("Transaction not found, status: pending")
                    return@withContext Result.success("pending")
                }

                // Check confirmation status
                val confirmationStatus = statusObj["confirmationStatus"]
                    ?.jsonPrimitive?.content ?: "pending"

                // Check if transaction failed
                val err = statusObj["err"]
                if (err != null && err !is kotlinx.serialization.json.JsonNull) {
                    Timber.w("Transaction failed with error: $err")
                    return@withContext Result.success("failed")
                }

                Timber.d("Transaction $signature status: $confirmationStatus")
                Result.success(confirmationStatus)

            } catch (e: Exception) {
                Timber.e(e, "Failed to get transaction status")
                Result.failure(e)
            }
        }
    }
}

data class SimulationResult(
    val willSucceed: Boolean,
    val errorMessage: String? = null
)