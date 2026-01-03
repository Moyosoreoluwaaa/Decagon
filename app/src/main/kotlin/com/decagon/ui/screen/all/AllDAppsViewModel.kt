package com.decagon.ui.screen.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.core.util.LoadingState
import com.decagon.domain.model.DApp
import com.decagon.domain.usecase.discover.ObserveDAppsUseCase
import com.decagon.domain.usecase.discover.RefreshDAppsUseCase
import com.decagon.ui.screen.discover.SortType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AllDAppsViewModel(
    private val observeDAppsUseCase: ObserveDAppsUseCase,
    private val refreshDAppsUseCase: RefreshDAppsUseCase
) : ViewModel() {

    private val TAG = "AllDAppsViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // All dApps (filtered by search)
    val allDApps: StateFlow<LoadingState<List<DApp>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            observeDAppsUseCase().map { state ->
                when (state) {
                    is LoadingState.Success -> {
                        if (query.isBlank()) {
                            state
                        } else {
                            LoadingState.Success(
                                state.data.filter {
                                    it.name.contains(query, ignoreCase = true)
                                }
                            )
                        }
                    }
                    else -> state
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LoadingState.Loading
        )

    private val _showScrollToTop = MutableStateFlow(false)
    val showScrollToTop: StateFlow<Boolean> = _showScrollToTop.asStateFlow()

    private val _showFilters = MutableStateFlow(false)
    val showFilters: StateFlow<Boolean> = _showFilters.asStateFlow()

    private val _sortType = MutableStateFlow(SortType.RANK)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    private val _showOnlyPositive = MutableStateFlow(false)
    val showOnlyPositive: StateFlow<Boolean> = _showOnlyPositive.asStateFlow()



    fun onSearchQueryChanged(query: String) {
        Timber.tag(TAG).d("🔍 Search query: '$query'")
        _searchQuery.value = query
    }

    fun refreshDApps() {
        if (_isRefreshing.value) {
            Timber.tag(TAG).w("⚠️ Refresh already in progress")
            return
        }

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                refreshDAppsUseCase()
                Timber.tag(TAG).i("✅ DApps refreshed")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ DApps refresh failed")
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