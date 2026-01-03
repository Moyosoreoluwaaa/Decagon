package com.decagon.domain.repository

import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Asset
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