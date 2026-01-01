package com.decagon.ui.screen.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octane.wallet.domain.models.Token
import com.octane.wallet.domain.usecases.discover.RefreshTokensUseCase
import com.octane.wallet.domain.usecases.discover.SearchTokensUseCase
import com.octane.wallet.presentation.viewmodel.SortType
import com.wallet.core.util.LoadingState
import com.wallet.domain.usecases.discover.ObserveAllTokensUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

class AllTokensViewModel(
    private val observeAllTokensUseCase: ObserveAllTokensUseCase,
    private val searchTokensUseCase: SearchTokensUseCase, // ✅ NOW USED
    private val refreshTokensUseCase: RefreshTokensUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val allTokens: StateFlow<LoadingState<List<Token>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            if (query.isBlank()) {
                observeAllTokensUseCase()
            } else {
                searchTokensUseCase(query) // ✅ FIXED: Now used
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    // In ViewModel class
    private val _showScrollToTop = MutableStateFlow(false)
    val showScrollToTop: StateFlow<Boolean> = _showScrollToTop.asStateFlow()

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.RANK)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _showOnlyPositive = MutableStateFlow(false)
    val showOnlyPositive: StateFlow<Boolean> = _showOnlyPositive.asStateFlow()


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

    fun onScrollStateChanged(firstVisibleIndex: Int, scrollOffset: Int) {
        _showScrollToTop.value = firstVisibleIndex > 5 || scrollOffset > 500
    }

    fun onToggleFilters() {
        _showFilters.value = !_showFilters.value
    }

    fun onSortTypeChanged(type: SortType) {
        _sortType.value = type
    }

    fun onTogglePositiveOnly() {
        _showOnlyPositive.value = !_showOnlyPositive.value
    }

    fun onResetFilters() {
        _searchQuery.value = ""
        _sortType.value = SortType.RANK
        _showOnlyPositive.value = false
    }
}
