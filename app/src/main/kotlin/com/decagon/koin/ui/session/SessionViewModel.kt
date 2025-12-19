package com.koin.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koin.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SessionViewModel (
    private val sessionManager: SessionManager
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false
    )

    suspend fun logout() = sessionManager.logout()
}
