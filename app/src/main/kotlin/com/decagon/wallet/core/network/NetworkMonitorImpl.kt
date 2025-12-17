package com.octane.wallet.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.core.content.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Android implementation of NetworkMonitor.
 * Monitors device connectivity using ConnectivityManager.
 *
 * CRITICAL: Initialization order matters!
 * 1. Declare StateFlows with simple initial values
 * 2. In init{}, update values AFTER all fields initialized
 * 3. Register callbacks LAST
 */
class NetworkMonitorImpl(
    context: Context
) : NetworkMonitor {

    private val connectivityManager = context.getSystemService<ConnectivityManager>()
        ?: throw IllegalStateException("ConnectivityManager not available")

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // ✅ Simple initial values - updated in init{}
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionType = MutableStateFlow(ConnectionType.NONE)
    override val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()

    override val isMetered: StateFlow<Boolean> = _connectionType
        .map { it == ConnectionType.CELLULAR }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isConnected.value = true
            updateConnectionType()
        }

        override fun onLost(network: Network) {
            _isConnected.value = false
            _connectionType.value = ConnectionType.NONE
        }

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            updateConnectionType()
        }
    }

    init {
        // ✅ STEP 1: Check initial connectivity state
        _isConnected.value = checkInitialConnectivity()

        // ✅ STEP 2: Determine initial connection type
        updateConnectionType()

        // ✅ STEP 3: Register callback (AFTER state initialized)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Modern API: Default network callback
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            // Legacy API: Explicit network request
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }
    }

    /**
     * Update connection type based on current network capabilities.
     * Called both during init and when network changes.
     */
    private fun updateConnectionType() {
        val network = connectivityManager.activeNetwork
        val capabilities = network?.let { connectivityManager.getNetworkCapabilities(it) }

        _connectionType.value = when {
            capabilities == null -> ConnectionType.NONE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                ConnectionType.ETHERNET
            else -> ConnectionType.NONE
        }
    }

    /**
     * Check if device is currently connected to internet.
     * Used during initialization.
     */
    private fun checkInitialConnectivity(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Cleanup: Unregister callback and cancel scope.
     * Call from Application.onTerminate() or DI cleanup.
     */
    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: IllegalArgumentException) {
            // Callback already unregistered, safe to ignore
        }
        scope.cancel()
    }
}