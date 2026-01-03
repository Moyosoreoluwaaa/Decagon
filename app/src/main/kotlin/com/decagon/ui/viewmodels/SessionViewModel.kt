package com.decagon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decagon.data.session.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class SessionViewModel (
    private val sessionManager: SessionManager
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn.stateIn(
        viewModelScope,
        SharingStarted.Companion.WhileSubscribed(5_000),
        false
    )

    suspend fun logout() = sessionManager.logout()
}