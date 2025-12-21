package com.wallet.data.repository

import com.octane.wallet.core.network.NetworkMonitor
import com.wallet.core.util.LoadingState
import com.octane.wallet.data.local.database.dao.AssetDao
import com.octane.wallet.data.mappers.toDomain
import com.wallet.data.local.database.entities.AssetEntity
import com.octane.wallet.data.remote.api.PriceApi
import com.octane.wallet.data.remote.api.SolanaRpcApi
import com.octane.wallet.data.remote.dto.price.PriceResponse
import com.octane.wallet.data.remote.dto.solana.RpcRequest
import com.octane.wallet.data.remote.dto.solana.TokenAccountsOptions
import com.octane.wallet.data.remote.dto.solana.TokenAccountsParams
import com.octane.wallet.domain.models.Asset
import com.wallet.domain.repository.AssetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Offline-first asset repository.
 * Pattern: Cache → Display → Refresh in background.
 */
class AssetRepositoryImpl(
    private val assetDao: AssetDao,
    private val solanaRpcApi: SolanaRpcApi,
    private val priceApi: PriceApi,
    private val networkMonitor: NetworkMonitor
) : AssetRepository {

    override fun observeAssets(walletId: String?): Flow<List<Asset>> {
        return assetDao.observeAssets(walletId.toString())
            .map { entities -> entities.map { it.toDomain() } }
    }


    override fun observeAsset(walletId: String, symbol: String): Flow<Asset?> {
        return assetDao.observeAsset(walletId, symbol)
            .map { it?.toDomain() }
    }

    override fun observeTotalValueUsd(walletId: String): Flow<Double?> {
        return assetDao.observeTotalValueUsd(walletId)
    }

    override suspend fun refreshAssets(
        walletId: String,
        publicKey: String
    ): LoadingState<Unit> {
        // Check network first
        if (!networkMonitor.isConnected.value) {
            return LoadingState.Error(
                throwable = Exception("No network connection"),
                message = "Working offline with cached data"
            )
        }

        return try {
            // 1. Fetch SOL balance
            val solBalance = fetchSolBalance(publicKey)

            // 2. Fetch SPL token balances
            val splTokens = fetchSplTokenBalances(publicKey)

            // 3. Fetch prices for all assets
            val allAssets = listOf(solBalance) + splTokens
            val prices = fetchPrices(allAssets.map { it.symbol })

            // 4. Merge prices with balances
            val assetsWithPrices = allAssets.map { asset ->
                val price = prices[asset.symbol.lowercase()]
                asset.copy(
                    priceUsd = price?.usd,
                    valueUsd = price?.usd?.let { it * asset.balance.toDouble() },
                    priceChange24h = price?.usd24hChange,
                    lastUpdated = System.currentTimeMillis()
                )
            }

            // 5. Save to database
            assetDao.insertAssets(assetsWithPrices)

            LoadingState.Success(Unit)
        } catch (e: Exception) {
            LoadingState.Error(e, "Failed to refresh assets: ${e.message}")
        }
    }

    override suspend fun updateAssetVisibility(assetId: String, isHidden: Boolean) {
        assetDao.updateVisibility(assetId, isHidden)
    }

    // Helper functions
    private suspend fun fetchSolBalance(publicKey: String): AssetEntity {
        val response = solanaRpcApi.getBalance(
            RpcRequest(
                method = "getBalance",
                params = listOf(publicKey)
            )
        )

        if (response.error != null) {
            throw Exception(response.error.message)
        }

        val lamports = response.result?.value ?: 0L
        val solBalance = lamports / 1_000_000_000.0

        return AssetEntity(
            id = UUID.randomUUID().toString(),
            walletId = "", // Set by caller
            chainId = "solana",
            symbol = "SOL",
            name = "Solana",
            mintAddress = null,
            balance = solBalance.toString(),
            decimals = 9,
            priceUsd = null,
            valueUsd = null,
            priceChange24h = null,
            iconUrl = null,
            isNative = true,
            isHidden = false,
            costBasisUsd = null,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private suspend fun fetchSplTokenBalances(publicKey: String): List<AssetEntity> {
        return try {
            val response = solanaRpcApi.getTokenAccountsByOwner(
                RpcRequest(
                    method = "getTokenAccountsByOwner",
                    params = TokenAccountsParams(
                        owner = publicKey,
                        options = TokenAccountsOptions()
                    )
                )
            )

            if (response.error != null) {
                throw Exception(response.error.message)
            }

            response.result?.value?.mapNotNull { account ->
                // Parse nested JSON structure safely
                val parsedData = account.account.data.parsed
                val tokenInfo = parsedData.info
                val tokenAmount = tokenInfo.tokenAmount
                val amount = tokenAmount.uiAmount ?: return@mapNotNull null

                if (amount == 0.0) return@mapNotNull null // Skip zero balances

                AssetEntity(
                    id = UUID.randomUUID().toString(),
                    walletId = "", // Set by caller
                    chainId = "solana",
                    symbol = "UNKNOWN", // Resolve via token list
                    name = "Unknown Token",
                    mintAddress = tokenInfo.mint,
                    balance = amount.toString(),
                    decimals = tokenAmount.decimals,
                    priceUsd = null,
                    valueUsd = null,
                    priceChange24h = null,
                    iconUrl = null,
                    isNative = false,
                    isHidden = false,
                    costBasisUsd = null,
                    lastUpdated = System.currentTimeMillis()
                )
            } ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun fetchPrices(symbols: List<String>): Map<String, PriceResponse> {
        return try {
            val coinIds = symbols.joinToString(",") { it.lowercase() }
            priceApi.getPrices(coinIds = coinIds)
        } catch (_: Exception) {
            emptyMap<String, PriceResponse>()
        }
    }
}