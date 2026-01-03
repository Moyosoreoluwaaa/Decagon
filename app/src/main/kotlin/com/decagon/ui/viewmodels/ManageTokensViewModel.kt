package com.decagon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.LoadingState
import com.decagon.domain.model.Asset
import com.decagon.domain.usecase.asset.ToggleAssetVisibilityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.filter

/**
 * Manage Tokens Screen ViewModel.
 * Allows users to show/hide assets from their portfolio.
 */
class ManageTokensViewModel(
    private val basePortfolio: BasePortfolioViewModel,
    private val toggleAssetVisibilityUseCase: ToggleAssetVisibilityUseCase
) : ViewModel() {

    // ==================== UI State ====================

    /**
     * Search query for filtering tokens.
     */
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * All assets (visible + hidden).
     * Delegated from BasePortfolioViewModel.
     */
    val allAssets: StateFlow<LoadingState<List<Asset>>> = basePortfolio.portfolioState
        .map { state ->
            when (state) {
                is LoadingState.Success -> {
                    // Include hidden assets for management
                    LoadingState.Success(state.data.assets)
                }

                else -> state as LoadingState<List<Asset>>
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            LoadingState.Loading
        )

    /**
     * Filtered assets based on search query.
     */
    val filteredAssets: StateFlow<LoadingState<List<Asset>>> = combine(
        allAssets,
        searchQuery
    ) { assets, query ->
        when (assets) {
            is LoadingState.Success -> {
                if (query.isBlank()) {
                    assets
                } else {
                    LoadingState.Success(
                        assets.data.filter {
                            it.name.contains(query, ignoreCase = true) ||
                                    it.symbol.contains(query, ignoreCase = true)
                        }
                    )
                }
            }

            else -> assets
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        LoadingState.Loading
    )

    // ==================== Actions ====================

    /**
     * Update search query.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Toggle asset visibility.
     */
    fun onToggleVisibility(assetId: String, isHidden: Boolean) {
        viewModelScope.launch {
            toggleAssetVisibilityUseCase(assetId, isHidden)
        }
    }

    /**
     * Clear search.
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }
}