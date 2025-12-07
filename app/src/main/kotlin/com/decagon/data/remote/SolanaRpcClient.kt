package com.decagon.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
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

            val response = httpClient.post(rpcUrl) {
                contentType(ContentType.Application.Json)
                setBody(buildJsonObject {
                    put("jsonrpc", "2.0")
                    put("id", UUID.randomUUID().toString())
                    put("method", "sendTransaction")
                    put("params", buildJsonArray {
                        add(base64Tx)
                    })
                })
            }

            val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            val signature = json["result"]?.jsonPrimitive?.content ?: throw IllegalStateException("No signature returned.")

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
}

data class SimulationResult(
    val willSucceed: Boolean,
    val errorMessage: String? = null
)