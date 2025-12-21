package com.octane.wallet.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.octane.wallet.core.network.ConnectionType
import com.octane.wallet.core.network.NetworkMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Android implementation of NetworkMonitor.
 * Uses ConnectivityManager to track network state changes.
 */
class NetworkMonitorImpl(
    context: Context
) : NetworkMonitor {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isConnected = MutableStateFlow(checkInitialConnection())
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectionType = MutableStateFlow(getCurrentConnectionType())
    override val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()

    private val _isMetered = MutableStateFlow(checkIfMetered())
    override val isMetered: StateFlow<Boolean> = _isMetered.asStateFlow()

    init {
        registerNetworkCallback()
    }

    private fun checkInitialConnection(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun getCurrentConnectionType(): ConnectionType {
        val activeNetwork = connectivityManager.activeNetwork ?: return ConnectionType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) 
            ?: return ConnectionType.NONE

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> ConnectionType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> ConnectionType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> ConnectionType.ETHERNET
            else -> ConnectionType.NONE
        }
    }

    private fun checkIfMetered(): Boolean {
        return connectivityManager.isActiveNetworkMetered
    }

    private fun registerNetworkCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(
            networkRequest,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    _isConnected.value = true
                    updateConnectionType()
                    updateMeteredStatus()
                }

                override fun onLost(network: Network) {
                    _isConnected.value = checkInitialConnection()
                    updateConnectionType()
                    updateMeteredStatus()
                }

                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    _isConnected.value = networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    ) && networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED
                    )
                    updateConnectionType()
                    updateMeteredStatus()
                }
            }
        )
    }

    private fun updateConnectionType() {
        _connectionType.value = getCurrentConnectionType()
    }

    private fun updateMeteredStatus() {
        _isMetered.value = checkIfMetered()
    }
}