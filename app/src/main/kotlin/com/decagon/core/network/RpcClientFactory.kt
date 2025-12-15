package com.decagon.core.network

import com.decagon.core.chains.ChainRegistry
import com.decagon.data.remote.SolanaRpcClient
import io.ktor.client.HttpClient
import timber.log.Timber

class RpcClientFactory(
    private val httpClient: HttpClient,
    private val networkManager: NetworkManager
) {
    fun createSolanaClient(chainId: String): SolanaRpcClient {
        val config = ChainRegistry.getChain(chainId)
        val url = config.networks.getUrl(networkManager.currentNetwork.value)
        Timber.d("Creating Solana RPC client: $chainId @ $url")
        return SolanaRpcClient(httpClient, url)
    }
}