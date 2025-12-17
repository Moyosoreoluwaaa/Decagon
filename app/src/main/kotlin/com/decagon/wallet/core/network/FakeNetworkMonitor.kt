package com.octane.wallet.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake NetworkMonitor for testing.
 * Allows simulating connectivity changes.
 */
class FakeNetworkMonitor : NetworkMonitor {
    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionType = MutableStateFlow(ConnectionType.WIFI)
    override val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()

    private val _isMetered = MutableStateFlow(false)
    override val isMetered: StateFlow<Boolean> = _isMetered.asStateFlow()

    // Test helpers
    fun simulateDisconnect() {
        _isConnected.value = false
        _connectionType.value = ConnectionType.NONE
    }

    fun simulateReconnect(type: ConnectionType = ConnectionType.WIFI) {
        _isConnected.value = true
        _connectionType.value = type
        _isMetered.value = (type == ConnectionType.CELLULAR)
    }

    fun simulateCellular() {
        _isConnected.value = true
        _connectionType.value = ConnectionType.CELLULAR
        _isMetered.value = true
    }

    fun reset() {
        _isConnected.value = true
        _connectionType.value = ConnectionType.WIFI
        _isMetered.value = false
    }
}