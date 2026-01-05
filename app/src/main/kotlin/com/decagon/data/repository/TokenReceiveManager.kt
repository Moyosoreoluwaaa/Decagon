package com.decagon.data.repository

import com.decagon.core.crypto.DecagonSplTokenProgram
import com.decagon.core.network.RpcClientFactory
import com.decagon.data.local.dao.TokenBalanceDao
import com.decagon.data.mapper.toDomain
import com.decagon.data.mapper.toEntity
import com.decagon.data.remote.api.JupiterUltraApiService
import com.decagon.domain.model.TokenBalance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sol4k.PublicKey
import timber.log.Timber

// ✅ INTERFACE (no constructor)
interface TokenReceiveManager {
    suspend fun discoverNewTokens(walletAddress: String): Result<List<TokenBalance>>
    suspend fun ensureTokenAccount(
        walletAddress: String,
        tokenMint: String,
        chainId: String = "solana"
    ): Result<String>
}

// ✅ IMPLEMENTATION (with constructor)
class TokenReceiveManagerImpl(
    private val jupiterApi: JupiterUltraApiService,
    private val tokenBalanceDao: TokenBalanceDao,
    private val splTokenProgram: DecagonSplTokenProgram,
    private val rpcFactory: RpcClientFactory
) : TokenReceiveManager {

    override suspend fun discoverNewTokens(
        walletAddress: String
    ): Result<List<TokenBalance>> = withContext(Dispatchers.IO) {
        try {
            Timber.d("🔍 Discovering tokens for: ${walletAddress.take(8)}...")

            val result = jupiterApi.getBalances(walletAddress)

            result.onSuccess { response ->
                val balances = response.balances?.map { it.toDomain() }
                val entities = balances?.map { it.toEntity(walletAddress) }
                tokenBalanceDao.insertAll(entities!!)

                Timber.i("✅ Discovered ${balances.size} tokens")
            }.onFailure { error ->
                Timber.e(error, "❌ Failed to discover tokens")
            }

            result.map { it.balances!!.map { dto -> dto.toDomain() } }
        } catch (e: Exception) {
            Timber.e(e, "❌ Exception discovering tokens")
            Result.failure(e)
        }
    }

    override suspend fun ensureTokenAccount(
        walletAddress: String,
        tokenMint: String,
        chainId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val ata = splTokenProgram.findAssociatedTokenAddress(
                walletAddress = PublicKey(walletAddress),
                tokenMintAddress = PublicKey(tokenMint)
            )

            val rpcClient = rpcFactory.createSolanaClient(chainId)
            val accountInfo = rpcClient.getAccountInfo(ata.toBase58())

            Result.success(ata.toBase58())
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to check ATA")
            Result.failure(e)
        }
    }
}