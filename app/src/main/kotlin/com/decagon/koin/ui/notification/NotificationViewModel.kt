
package com.koin.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koin.data.notification.NotificationRepository
import com.koin.domain.notification.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel (
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getNotifications().collect { notifications ->
                _notifications.value = notifications
            }
        }
        viewModelScope.launch {
            repository.getUnreadCount().collect { count ->
                _unreadCount.value = count
            }
        }
    }

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun addNotification(notification: Notification) {
        viewModelScope.launch {
            repository.insert(notification)
        }
    }
}
