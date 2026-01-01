package com.octane.wallet.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.models.Token
import com.octane.wallet.domain.usecases.discover.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class DiscoverViewModel(
    private val observeTrendingTokensUseCase: ObserveTrendingTokensUseCase,
    private val searchTokensUseCase: SearchTokensUseCase,
    private val refreshTokensUseCase: RefreshTokensUseCase,
    private val observePerpsUseCase: ObservePerpsUseCase,
    private val searchPerpsUseCase: SearchPerpsUseCase,
    private val refreshPerpsUseCase: RefreshPerpsUseCase,
    private val observeDAppsUseCase: ObserveDAppsUseCase,
    private val searchDAppsUseCase: SearchDAppsUseCase,
    private val refreshDAppsUseCase: RefreshDAppsUseCase
) : ViewModel() {

    private val TAG = "DiscoverViewModel"

    init {
        Timber.tag(TAG).d("🎬 DiscoverViewModel initialized")
    }

    // ==================== CORE STATE ====================

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMode = MutableStateFlow<DiscoverMode>(DiscoverMode.TOKENS)
    val selectedMode: StateFlow<DiscoverMode> = _selectedMode.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ==================== NEW: UX STATE ====================

    // Toolbar collapse state
    private val _showToolbar = MutableStateFlow(true)
    val showToolbar: StateFlow<Boolean> = _showToolbar.asStateFlow()

    // Scroll-to-top FAB visibility
    private val _showScrollToTop = MutableStateFlow(false)
    val showScrollToTop: StateFlow<Boolean> = _showScrollToTop.asStateFlow()

    // Filter panel expansion
    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    // Sorting
    private val _sortType = MutableStateFlow(SortType.RANK)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    // Positive-only filter
    private val _showOnlyPositive = MutableStateFlow(false)
    val showOnlyPositive: StateFlow<Boolean> = _showOnlyPositive.asStateFlow()

    // ==================== TOKENS ====================

    val trendingTokens: StateFlow<LoadingState<List<Token>>> = observeTrendingTokensUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Timber.tag(TAG).d("🔵 Trending tokens - Loading")
                is LoadingState.Success -> {
                    Timber.tag(TAG).i("✅ Trending tokens - Success: ${state.data.size} items")
                }

                is LoadingState.Error -> Timber.tag(TAG)
                    .e("❌ Trending tokens - Error: ${state.message}")

                else -> {}
            }
        }
        .map { state -> applySortingAndFilters(state) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val tokenSearchResults: StateFlow<LoadingState<List<Token>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            Timber.tag(TAG).d("🔎 Searching tokens: '$query'")
            searchTokensUseCase(query)
        }
        .map { state -> applySortingAndFilters(state) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== PERPS ====================

    val perps: StateFlow<LoadingState<List<Perp>>> = observePerpsUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Timber.tag(TAG).d("🔵 Perps - Loading")
                is LoadingState.Success -> Timber.tag(TAG)
                    .i("✅ Perps - Success: ${state.data.size} items")

                is LoadingState.Error -> Timber.tag(TAG).e("❌ Perps - Error: ${state.message}")
                else -> {}
            }
        }
        .map { state -> applySortingAndFiltersPerps(state) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val perpSearchResults: StateFlow<LoadingState<List<Perp>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            Timber.tag(TAG).d("🔎 Searching perps: '$query'")
            searchPerpsUseCase(query)
        }
        .map { state -> applySortingAndFiltersPerps(state) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== DAPPS ====================

    val dapps: StateFlow<LoadingState<List<DApp>>> = observeDAppsUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Timber.tag(TAG).d("🔵 DApps - Loading")
                is LoadingState.Success -> Timber.tag(TAG)
                    .i("✅ DApps - Success: ${state.data.size} items")

                is LoadingState.Error -> Timber.tag(TAG).e("❌ DApps - Error: ${state.message}")
                else -> {}
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val dappSearchResults: StateFlow<LoadingState<List<DApp>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .flatMapLatest { query ->
            Timber.tag(TAG).d("🔎 Searching dApps: '$query'")
            searchDAppsUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== SORTING & FILTERING ====================

    private fun applySortingAndFilters(state: LoadingState<List<Token>>): LoadingState<List<Token>> {
        if (state !is LoadingState.Success) return state

        var filtered = state.data

        // Apply positive-only filter
        if (_showOnlyPositive.value) {
            filtered = filtered.filter { it.priceChange24h > 0 }
        }

        // Apply sorting
        filtered = when (_sortType.value) {
            SortType.RANK -> filtered // Already sorted by rank
            SortType.NAME -> filtered.sortedBy { it.name }
            SortType.PRICE -> filtered.sortedByDescending { it.currentPrice }
            SortType.CHANGE -> filtered.sortedByDescending { it.priceChange24h }
            SortType.MARKET_CAP -> filtered.sortedByDescending { it.marketCap }
        }

        return LoadingState.Success(filtered)
    }

    private fun applySortingAndFiltersPerps(state: LoadingState<List<Perp>>): LoadingState<List<Perp>> {
        if (state !is LoadingState.Success) return state

        var filtered = state.data

        // Apply positive-only filter
        if (_showOnlyPositive.value) {
            filtered = filtered.filter { it.priceChange24h > 0 }
        }

        // Apply sorting
        filtered = when (_sortType.value) {
            SortType.RANK -> filtered
            SortType.NAME -> filtered.sortedBy { it.name }
            SortType.PRICE -> filtered.sortedByDescending { it.indexPrice }
            SortType.CHANGE -> filtered.sortedByDescending { it.priceChange24h }
            SortType.MARKET_CAP -> filtered.sortedByDescending { it.openInterest }
        }

        return LoadingState.Success(filtered)
    }

    // ==================== EVENTS ====================

    fun onSearchQueryChanged(query: String) {
        Timber.tag(TAG).d("🔍 Search query: '$query'")
        _searchQuery.value = query
    }

    fun onModeSelected(mode: DiscoverMode) {
        if (_selectedMode.value == mode) return
        Timber.tag(TAG).i("🎯 Mode selected: $mode")
        _selectedMode.value = mode
    }

    fun onScrollStateChanged(firstVisibleIndex: Int, scrollOffset: Int) {
        // Collapse toolbar when scrolled past threshold
        val shouldShowToolbar = firstVisibleIndex == 0 && scrollOffset < 100
        if (_showToolbar.value != shouldShowToolbar) {
            _showToolbar.value = shouldShowToolbar
        }

        // Show FAB when scrolled down significantly
        val shouldShowFab = firstVisibleIndex > 0 || scrollOffset > 300
        if (_showScrollToTop.value != shouldShowFab) {
            _showScrollToTop.value = shouldShowFab
        }
    }

    fun onToggleFilters() {
        _showFilters.value = !_showFilters.value
        Timber.tag(TAG).d("🎛️ Filters toggled: ${_showFilters.value}")
    }

    fun onSortTypeChanged(type: SortType) {
        _sortType.value = type
        Timber.tag(TAG).d("🔀 Sort type: $type")
    }

    fun onTogglePositiveOnly() {
        _showOnlyPositive.value = !_showOnlyPositive.value
        Timber.tag(TAG).d("➕ Show only positive: ${_showOnlyPositive.value}")
    }

    fun onResetFilters() {
        _searchQuery.value = ""
        _sortType.value = SortType.RANK
        _showOnlyPositive.value = false
        _showFilters.value = false
        Timber.tag(TAG).d("🔄 Filters reset")
    }

    fun refreshAll() {
        if (_isRefreshing.value) {
            Timber.tag(TAG).w("⚠️ Refresh already in progress")
            return
        }

        Timber.tag(TAG).i("🔄 Manual refresh triggered")
        when (_selectedMode.value) {
            DiscoverMode.TOKENS -> refreshTokens()
            DiscoverMode.PERPS -> refreshPerps()
            DiscoverMode.LISTS -> refreshDApps()
        }
    }

    private fun refreshTokens() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val result = refreshTokensUseCase()
                when (result) {
                    is LoadingState.Success -> Timber.tag(TAG).i("✅ Tokens refreshed")
                    is LoadingState.Error -> Timber.tag(TAG)
                        .e("❌ Token refresh failed: ${result.message}")

                    else -> {}
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ Token refresh exception")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun refreshPerps() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                refreshPerpsUseCase()
                Timber.tag(TAG).i("✅ Perps refreshed")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ Perps refresh exception")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun refreshDApps() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                refreshDAppsUseCase()
                Timber.tag(TAG).i("✅ DApps refreshed")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ DApps refresh exception")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onTokenClicked(token: Token) {
        Timber.tag(TAG).d("🎯 Token clicked: ${token.symbol}")
    }

    fun onPerpClicked(perp: Perp) {
        Timber.tag(TAG).d("🎯 Perp clicked: ${perp.symbol}")
    }

    fun onDAppClicked(dapp: DApp) {
        Timber.tag(TAG).d("🎯 DApp clicked: ${dapp.name}")
    }

    // NEW: Favorite/Buy actions (to be implemented with use cases later)
    fun onTokenFavorited(token: Token) {
        Timber.tag(TAG).d("⭐ Token favorited: ${token.symbol}")
        // TODO: Implement with FavoriteTokenUseCase
    }

    fun onTokenBuyClicked(token: Token) {
        Timber.tag(TAG).d("🛒 Buy clicked: ${token.symbol}")
        // TODO: Show buy bottom sheet
    }

    override fun onCleared() {
        super.onCleared()
        Timber.tag(TAG).d("🧹 DiscoverViewModel cleared")
    }
}

// ==================== ENUMS ====================

enum class DiscoverMode {
    TOKENS,
    PERPS,
    LISTS
}

enum class SortType {
    RANK,
    NAME,
    PRICE,
    CHANGE,
    MARKET_CAP
}

// ==================== SEARCH SUGGESTIONS ====================

sealed class SearchSuggestion {
    abstract val displayName: String
    abstract val subtitle: String

    data class TokenSuggestion(val token: Token) : SearchSuggestion() {
        override val displayName: String get() = token.name
        override val subtitle: String get() = token.symbol.uppercase()
    }

    data class PerpSuggestion(val perp: Perp) : SearchSuggestion() {
        override val displayName: String get() = perp.name
        override val subtitle: String get() = perp.symbol.uppercase()
    }

    data class DAppSuggestion(val dapp: DApp) : SearchSuggestion() {
        override val displayName: String get() = dapp.name
        override val subtitle: String get() = dapp.category.name
    }
}