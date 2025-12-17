package com.octane.wallet.domain.repository

import com.octane.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Asset
import kotlinx.coroutines.flow.Flow

/**
 * Offline-first asset repository.
 * Pattern: Cache → Display → Refresh in background.
 */

interface AssetRepository {
    fun observeAssets(walletId: String? = null): Flow<List<Asset>>
    fun observeAsset(walletId: String, symbol: String): Flow<Asset?>
    fun observeTotalValueUsd(walletId: String): Flow<Double?>
    suspend fun refreshAssets(walletId: String, publicKey: String): LoadingState<Unit>
    suspend fun updateAssetVisibility(assetId: String, isHidden: Boolean)
}