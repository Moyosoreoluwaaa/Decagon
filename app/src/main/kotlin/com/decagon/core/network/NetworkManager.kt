package com.decagon.core.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

interface NetworkManager {
    val currentNetwork: StateFlow<NetworkEnvironment>
    suspend fun switchNetwork(network: NetworkEnvironment)
}

class NetworkManagerImpl(
    private val dataStore: DataStore<Preferences>
) : NetworkManager {
    
    private val scope = CoroutineScope(SupervisorJob())
    
    private val _currentNetwork = MutableStateFlow(NetworkEnvironment.DEVNET)
    override val currentNetwork: StateFlow<NetworkEnvironment> = _currentNetwork.asStateFlow()
    
    init {
        scope.launch {
            val saved = dataStore.data
                .map { it[NETWORK_KEY] ?: "devnet" }
                .first()
            _currentNetwork.value = NetworkEnvironment.fromString(saved)
            Timber.d("Network initialized: ${_currentNetwork.value}")
        }
    }
    
    override suspend fun switchNetwork(network: NetworkEnvironment) {
        Timber.i("Switching network: ${_currentNetwork.value} -> $network")
        _currentNetwork.value = network
        dataStore.edit { it[NETWORK_KEY] = network.name.lowercase() }
    }
    
    companion object {
        private val NETWORK_KEY = stringPreferencesKey("selected_network")
    }
}