package com.koin.ui.profile

import androidx.lifecycle.viewModelScope
import com.koin.data.session.SessionManager
import com.koin.domain.user.User
import com.koin.domain.user.UserRepository
import com.koin.domain.watchlist.WatchlistItem
import com.koin.domain.watchlist.WatchlistRepository
import com.koin.ui.base.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: UserRepository,
    private val watchlistRepository: WatchlistRepository,
    private val sessionManager: SessionManager
) : BaseViewModel<ProfileUiState, ProfileUiEvent>() {

    override val _uiState: MutableStateFlow<ProfileUiState> = MutableStateFlow(ProfileUiState())

    init {
        loadUser()
        loadWatchlist()
    }

    private fun loadUser() {
        // Assume single user with id = 1 for now
        viewModelScope.launch {
            repository.user(1L)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collectLatest { user ->
                    _uiState.update { it.copy(user = user, isLoading = false) }
                }
        }
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            watchlistRepository.getWatchlistForUser(1L) // Assume single user with id = 1
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collectLatest { watchlist ->
                    _uiState.update { it.copy(watchlist = watchlist) }
                }
        }
    }

    override fun handleEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.Save -> save(event.name, event.email, event.bio, event.avatarUri)
            ProfileUiEvent.Refresh -> {
                loadUser()
                loadWatchlist()
            }
            is ProfileUiEvent.RemoveFromWatchlist -> removeFromWatchlist(event.coinId)
        }
    }

    private fun save(name: String, email: String, bio: String?, avatarUri: String?) {
        viewModelScope.launch {
            try {
                val user = User(
                    id = _uiState.value.user?.id ?: 0,
                    name = name,
                    email = email,
                    bio = bio,
                    avatarUri = avatarUri
                )
                repository.upsert(user)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun removeFromWatchlist(coinId: String) {
        viewModelScope.launch {
            try {
                watchlistRepository.removeFromWatchlist(1L, coinId) // Assume single user with id = 1
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to remove from watchlist: ${e.message}") }
            }
        }
    }

    
}

data class ProfileUiState(
    val user: User? = null,
    val watchlist: List<WatchlistItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val loggedOut: Boolean = false
)


sealed class ProfileUiEvent {
    data class Save(
        val name: String,
        val email: String,
        val bio: String?,
        val avatarUri: String?
    ) : ProfileUiEvent()

    object Refresh : ProfileUiEvent()
    data class RemoveFromWatchlist(val coinId: String) : ProfileUiEvent()
}
