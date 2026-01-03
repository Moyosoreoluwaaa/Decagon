package com.decagon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.LoadingState
import com.decagon.domain.model.DApp
import com.decagon.domain.model.Perp
import com.decagon.domain.model.Token
import com.decagon.domain.usecase.discover.ObserveDAppsUseCase
import com.decagon.domain.usecase.discover.ObservePerpsUseCase
import com.decagon.domain.usecase.discover.ObserveTrendingTokensUseCase
import com.decagon.domain.usecase.discover.RefreshDAppsUseCase
import com.decagon.domain.usecase.discover.RefreshPerpsUseCase
import com.decagon.domain.usecase.discover.RefreshTokensUseCase
import com.decagon.domain.usecase.discover.SearchDAppsUseCase
import com.decagon.domain.usecase.discover.SearchPerpsUseCase
import com.decagon.domain.usecase.discover.SearchTokensUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
        Timber.tag(TAG).d("🎬 Use cases injected successfully")
    }

    // ==================== UI State ====================

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedMode = MutableStateFlow<DiscoverMode>(DiscoverMode.TOKENS)
    val selectedMode: StateFlow<DiscoverMode> = _selectedMode.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ==================== TOKENS ====================

    val trendingTokens: StateFlow<LoadingState<List<Token>>> = observeTrendingTokensUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Timber.tag(TAG)
                    .d("🔵 ViewModel: Trending tokens - Loading")

                is LoadingState.Success -> {
                    Timber.tag(TAG)
                        .i("✅ ViewModel: Trending tokens - Success with ${state.data.size} items")
                    if (state.data.isNotEmpty()) {
                        Timber.tag(TAG).d(
                            "📋 Top 3 tokens: ${
                                state.data.take(3).map { "${it.symbol}=${it.formattedPrice}" }
                            }")
                    }
                }

                is LoadingState.Error -> Timber.tag(TAG)
                    .e("❌ ViewModel: Trending tokens - Error: ${state.message}")

                else -> Timber.tag(TAG).d("⚪ ViewModel: Trending tokens - Idle/Unknown state")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    val tokenSearchResults: StateFlow<LoadingState<List<Token>>> = _searchQuery
        .onEach { query ->
            Timber.tag(TAG).d("🔍 Search query changed: '$query'")
        }
        .debounce(300)
        .onEach { query ->
            Timber.tag(TAG).d("⏰ Debounced search query: '$query'")
        }
        .distinctUntilChanged()
        .filter { it.isNotBlank() }
        .onEach { query ->
            Timber.tag(TAG).d("🔎 Executing search for: '$query'")
        }
        .flatMapLatest { query ->
            searchTokensUseCase(query)
                .onEach { state ->
                    when (state) {
                        is LoadingState.Success -> {
                            Timber.tag(TAG).d("✅ Search results: ${state.data.size} tokens found")
                        }

                        is LoadingState.Error -> {
                            Timber.tag(TAG).e("❌ Search error: ${state.message}")
                        }

                        else -> Timber.tag(TAG).d("🔵 Search loading...")
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== PERPS ====================

    val perps: StateFlow<LoadingState<List<Perp>>> = observePerpsUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Timber.tag(TAG).d("🔵 ViewModel: Perps - Loading")
                is LoadingState.Success -> Timber.tag(TAG)
                    .i("✅ ViewModel: Perps - Success with ${state.data.size} items")

                is LoadingState.Error -> Timber.tag(TAG)
                    .e("❌ ViewModel: Perps - Error: ${state.message}")

                else -> {}
            }
        }
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
            Timber.tag(TAG).d("🔎 Searching perps for: '$query'")
            searchPerpsUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== DAPPS ====================

    val dapps: StateFlow<LoadingState<List<DApp>>> = observeDAppsUseCase()
        .onEach { state ->
            when (state) {
                is LoadingState.Loading -> Timber.tag(TAG).d("🔵 ViewModel: DApps - Loading")
                is LoadingState.Success -> Timber.tag(TAG)
                    .i("✅ ViewModel: DApps - Success with ${state.data.size} items")

                is LoadingState.Error -> Timber.tag(TAG)
                    .e("❌ ViewModel: DApps - Error: ${state.message}")

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
            Timber.tag(TAG).d("🔎 Searching dApps for: '$query'")
            searchDAppsUseCase(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // ==================== Actions ====================

    fun onSearchQueryChanged(query: String) {
        Timber.tag(TAG).d("📝 onSearchQueryChanged: '$query'")
        _searchQuery.value = query
    }

    fun onModeSelected(mode: DiscoverMode) {
        Timber.tag(TAG).i("🎯 Mode selected: $mode (previous: ${_selectedMode.value})")
        _selectedMode.value = mode

        when (mode) {
            DiscoverMode.TOKENS -> {
                Timber.tag(TAG).d("🪙 Switched to Tokens mode, triggering refresh")
                refreshTokens()
            }

            DiscoverMode.PERPS -> {
                Timber.tag(TAG).d("📈 Switched to Perps mode, triggering refresh")
                refreshPerps()
            }

            DiscoverMode.LISTS -> {
                Timber.tag(TAG).d("📱 Switched to Lists mode, triggering refresh")
                refreshDApps()
            }
        }
    }

    fun clearSearch() {
        Timber.tag(TAG).d("🗑️ Clearing search query")
        _searchQuery.value = ""
    }

    fun refresh() {
        if (_isRefreshing.value) {
            Timber.tag(TAG).w("⚠️ Refresh already in progress, ignoring")
            return
        }

        Timber.tag(TAG).i("🔄 Manual refresh triggered for mode: ${_selectedMode.value}")

        when (_selectedMode.value) {
            DiscoverMode.TOKENS -> refreshTokens()
            DiscoverMode.PERPS -> refreshPerps()
            DiscoverMode.LISTS -> refreshDApps()
        }
    }

    private fun refreshTokens() {
        Timber.tag(TAG).d("🔄 refreshTokens() called")

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Timber.tag(TAG).d("🔄 Starting token refresh...")

                val result = refreshTokensUseCase()

                when (result) {
                    is LoadingState.Success -> {
                        Timber.tag(TAG).i("✅ Token refresh completed successfully")
                    }

                    is LoadingState.Error -> {
                        Timber.tag(TAG).e("❌ Token refresh failed: ${result.message}")
                    }

                    else -> {
                        Timber.tag(TAG).w("⚠️ Token refresh returned unexpected state: $result")
                    }
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ Exception during token refresh")
            } finally {
                _isRefreshing.value = false
                Timber.tag(TAG).d("🔄 Token refresh completed, isRefreshing set to false")
            }
        }
    }

    private fun refreshPerps() {
        Timber.tag(TAG).d("🔄 refreshPerps() called")

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Timber.tag(TAG).d("🔄 Starting perps refresh...")

                refreshPerpsUseCase()

                Timber.tag(TAG).i("✅ Perps refresh completed")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ Exception during perps refresh")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun refreshDApps() {
        Timber.tag(TAG).d("🔄 refreshDApps() called")

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                Timber.tag(TAG).d("🔄 Starting dApps refresh...")

                refreshDAppsUseCase()

                Timber.tag(TAG).i("✅ dApps refresh completed")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ Exception during dApps refresh")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onTokenClicked(token: Token) {
        Timber.tag(TAG).d("🎯 Token clicked: ${token.symbol}")
        // Navigation handled in DiscoverScreen via onNavigateToTokenDetails
    }

    fun onPerpClicked(perp: Perp) {
        Timber.tag(TAG).d("🎯 Perp clicked: ${perp.symbol}")
        // Navigation handled in DiscoverScreen via callback
    }

    fun onDAppClicked(dapp: DApp) {
        Timber.tag(TAG).d("🎯 DApp clicked: ${dapp.name}")
        // TODO: Open dApp URL in browser or WebView
    }

    override fun onCleared() {
        super.onCleared()
        Timber.tag(TAG).d("🧹 DiscoverViewModel cleared")
    }
}

enum class DiscoverMode {
    TOKENS,
    PERPS,
    LISTS
}