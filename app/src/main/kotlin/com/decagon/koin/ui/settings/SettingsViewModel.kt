
package com.koin.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koin.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _loggedOut = MutableStateFlow(false)
    val loggedOut = _loggedOut.asStateFlow()

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
            _loggedOut.value = true
        }
    }
}
