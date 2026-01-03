package com.decagon.core.network

import kotlinx.coroutines.flow.StateFlow

/**
 * Monitors device network connectivity and type.
 * Used to show offline banners and enable offline-first patterns.
 */
interface NetworkMonitor {
    /**
     * Reactive connectivity state.
     * Emit true when online, false when offline.
     */
    val isConnected: StateFlow<Boolean>

    /**
     * Current connection type (WiFi, Cellular, etc.)
     */
    val connectionType: StateFlow<ConnectionType>

    /**
     * Whether connection is metered (cellular, limited WiFi).
     * Use to warn before large downloads/syncs.
     */
    val isMetered: StateFlow<Boolean>
}

enum class ConnectionType {
    WIFI,       // Fast, typically unlimited
    CELLULAR,   // Metered, may be slow
    ETHERNET,   // Fast, unlimited
    NONE        // Offline
}