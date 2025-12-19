package com.koin.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract   class BaseViewModel<State, Event> : ViewModel() {
    protected abstract val _uiState: MutableStateFlow<State>
    val uiState: StateFlow<State>
        get() = _uiState // ✅ defers access until subclass has initialized it


    protected abstract fun handleEvent(event: Event)

    fun onEvent(event: Event) {
        viewModelScope.launch {
            handleEvent(event)
        }
    }

    protected inline fun <T> execute(
        crossinline onStart: () -> Unit = {},
        crossinline onComplete: () -> Unit = {},
        crossinline onError: (Throwable) -> Unit = {},
        crossinline block: suspend () -> T
    ) {
        viewModelScope.launch {
            try {
                onStart()
                block()
            } catch (e: Exception) {
                onError(e)
            } finally {
                onComplete()
            }
        }
    }
}
