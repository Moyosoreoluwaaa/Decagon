package com.koin.ui.auth

import com.koin.data.session.SessionManager
import com.koin.domain.user.User
import com.koin.domain.user.UserRepository
import com.koin.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : BaseViewModel<AuthUiState, AuthUiEvent>() {

    override val _uiState: MutableStateFlow<AuthUiState> = MutableStateFlow(AuthUiState())

    override fun handleEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.Register -> register(event.name, event.email, event.bio)
        }
    }

    private fun register(name: String, email: String, bio: String?) {
        execute(onError = { e ->
            _uiState.update { it.copy(error = e.message) }
        }) {
            val user = User(name = name, email = email, bio = bio)
            val insertedId = userRepository.upsert(user)
            sessionManager.login(insertedId)
            _uiState.update { it.copy(isRegistered = true) }
        }
    }
}

data class AuthUiState(
    val isRegistered: Boolean = false,
    val error: String? = null
)

sealed class AuthUiEvent {
    data class Register(val name: String, val email: String, val bio: String?) : AuthUiEvent()
}
