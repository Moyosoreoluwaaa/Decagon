package com.decagon.ui.screen.all

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.domain.usecase.ObserveAllPerpsUseCase
import com.wallet.core.util.LoadingState
import com.octane.wallet.domain.models.Perp
import com.octane.wallet.domain.usecases.discover.RefreshPerpsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class AllPerpsViewModel(
    private val observeAllPerpsUseCase: ObserveAllPerpsUseCase, // ✅ FIXED: Use dedicated use case
    private val refreshPerpsUseCase: RefreshPerpsUseCase
) : ViewModel() {

    private val TAG = "AllPerpsViewModel"

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ✅ FIXED: Now uses observeAllPerpsUseCase (unlimited)
    val allPerps: StateFlow<LoadingState<List<Perp>>> = _searchQuery
        .debounce(300)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            observeAllPerpsUseCase().map { state ->
                when (state) {
                    is LoadingState.Success -> {
                        if (query.isBlank()) {
                            state
                        } else {
                            LoadingState.Success(
                                state.data.filter {
                                    it.name.contains(query, ignoreCase = true) ||
                                            it.symbol.contains(query, ignoreCase = true)
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

    fun refreshPerps() {
        if (_isRefreshing.value) {
            Timber.tag(TAG).w("⚠️ Refresh already in progress")
            return
        }

        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                refreshPerpsUseCase()
                Timber.tag(TAG).i("✅ Perps refreshed")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ Perps refresh failed")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}