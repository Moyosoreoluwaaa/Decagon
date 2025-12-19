
package com.koin.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.koin.data.notification.NotificationRepository
import com.koin.domain.notification.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationDetailViewModel (
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notification = MutableStateFlow<Notification?>(null)
    val notification: StateFlow<Notification?> = _notification.asStateFlow()

    fun getNotification(id: Long) {
        viewModelScope.launch {
            repository.getNotifications().collect { notifications ->
                _notification.value = notifications.find { it.id == id }
                _notification.value?.let { if (!it.isRead) repository.markAsRead(it.id) }
            }
        }
    }
}
