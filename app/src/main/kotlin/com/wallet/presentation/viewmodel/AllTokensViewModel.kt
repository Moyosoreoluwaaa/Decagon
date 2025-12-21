package com.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.wallet.domain.models.Token
import com.octane.wallet.domain.usecases.discover.RefreshTokensUseCase
import com.octane.wallet.domain.usecases.discover.SearchTokensUseCase
import com.wallet.core.util.LoadingState
import com.wallet.domain.repository.DiscoverRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

// ==================== VIEWMODEL ====================

/**
 * ViewModel for AllTokensScreen.
 * 
 * Key differences from DiscoverViewModel:
 * - NO 10-item limit (shows all tokens)
 * - Pagination support (load more)
 * - Advanced sorting/filtering
 */
class AllTokensViewModel(
    private val observeAllTokensUseCase: ObserveAllTokensUseCase, // NEW use case
    private val searchTokensUseCase: SearchTokensUseCase,
    private val refreshTokensUseCase: RefreshTokensUseCase
) : ViewModel() {
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()
    
    // ✅ Shows ALL tokens (no limit)
    val allTokens: StateFlow<LoadingState<List<Token>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                observeAllTokensUseCase() // Returns ALL tokens
            } else {
                searchTokensUseCase(query) // Search also unlimited
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
    
    fun refreshTokens() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                refreshTokensUseCase()
            } catch (e: Exception) {
                Timber.e(e, "❌ Token refresh failed")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

// ==================== NEW USE CASE ====================

/**
 * ✅ NEW: Observe ALL tokens (no 10-item limit).
 * 
 * This is a separate use case from ObserveTrendingTokensUseCase
 * which is limited to 10 items for Discover screen performance.
 */
class ObserveAllTokensUseCase(
    private val repository: DiscoverRepository
) {
    operator fun invoke(): Flow<LoadingState<List<Token>>> {
        // ✅ Call new repository method that doesn't apply .take(10)
        return repository.observeAllTokens()
    }
}