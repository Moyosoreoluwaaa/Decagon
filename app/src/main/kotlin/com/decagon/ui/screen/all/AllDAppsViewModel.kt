package com.decagon.ui.screen.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.DApp
import com.octane.wallet.domain.usecases.discover.ObserveDAppsUseCase
import com.octane.wallet.domain.usecases.discover.RefreshDAppsUseCase
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
}