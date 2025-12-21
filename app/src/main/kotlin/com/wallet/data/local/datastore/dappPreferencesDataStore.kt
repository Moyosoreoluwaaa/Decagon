package com.octane.wallet.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.octane.wallet.presentation.viewmodel.Bookmark
import com.octane.wallet.presentation.viewmodel.ConnectedWallet
import com.octane.wallet.presentation.viewmodel.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ✅ DataStore extension for Context (DApps)
private val Context.dappPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dapp_preferences"
)

/**
 * Interface for managing DApp-related local preferences (connections, bookmarks, history).
 */
interface DAppPreferencesStore {
    // ==================== WALLET CONNECTIONS ====================
    suspend fun saveWalletConnection(dappUrl: String, walletId: String, publicKey: String)
    suspend fun getWalletConnection(dappUrl: String): ConnectedWallet?
    suspend fun removeWalletConnection(dappUrl: String)

    // ==================== BOOKMARKS ====================
    suspend fun addBookmark(url: String, title: String)
    suspend fun removeBookmark(url: String)
    suspend fun isBookmarked(url: String): Boolean
    fun observeBookmarks(): Flow<List<Bookmark>>

    // ==================== HISTORY ====================
    suspend fun addHistory(url: String, title: String, timestamp: Long)
    fun observeHistory(): Flow<List<HistoryItem>>
    suspend fun clearHistory()
}

/**
 * Implementation of DAppPreferencesStore using DataStore.
 */
class DAppPreferencesStoreImpl(private val context: Context) : DAppPreferencesStore {

    private val dataStore = context.dappPreferencesDataStore

    // ==================== WALLET CONNECTIONS ====================

    override suspend fun saveWalletConnection(
        dappUrl: String,
        walletId: String,
        publicKey: String
    ) {
        val key = stringPreferencesKey("wallet_${sanitizeUrl(dappUrl)}")
        dataStore.edit { prefs ->
            prefs[key] = Json.encodeToString(
                ConnectedWalletData(
                    walletId = walletId,
                    publicKey = publicKey,
                    connectedAt = System.currentTimeMillis()
                )
            )
        }
    }

    override suspend fun getWalletConnection(dappUrl: String): ConnectedWallet? {
        val key = stringPreferencesKey("wallet_${sanitizeUrl(dappUrl)}")
        return dataStore.data.map { prefs ->
            prefs[key]?.let { json ->
                val data = Json.decodeFromString<ConnectedWalletData>(json)
                ConnectedWallet(
                    walletId = data.walletId,
                    publicKey = data.publicKey,
                    connectedAt = data.connectedAt
                )
            }
        }.first()
    }

    override suspend fun removeWalletConnection(dappUrl: String) {
        val key = stringPreferencesKey("wallet_${sanitizeUrl(dappUrl)}")
        dataStore.edit { it.remove(key) }
    }

    // ==================== BOOKMARKS ====================

    private val bookmarksKey = stringPreferencesKey("bookmarks")

    override suspend fun addBookmark(url: String, title: String) {
        dataStore.edit { prefs ->
            val currentBookmarks = prefs[bookmarksKey]?.let { json ->
                Json.decodeFromString<List<BookmarkData>>(json)
            } ?: emptyList()

            val newBookmark = BookmarkData(
                url = url,
                title = title,
                addedAt = System.currentTimeMillis()
            )

            prefs[bookmarksKey] = Json.encodeToString(currentBookmarks + newBookmark)
        }
    }

    override suspend fun removeBookmark(url: String) {
        dataStore.edit { prefs ->
            val currentBookmarks = prefs[bookmarksKey]?.let { json ->
                Json.decodeFromString<List<BookmarkData>>(json)
            } ?: emptyList()

            prefs[bookmarksKey] = Json.encodeToString(
                currentBookmarks.filter { it.url != url }
            )
        }
    }

    override suspend fun isBookmarked(url: String): Boolean {
        return dataStore.data.map { prefs ->
            prefs[bookmarksKey]?.let { json ->
                val bookmarks = Json.decodeFromString<List<BookmarkData>>(json)
                bookmarks.any { it.url == url }
            } ?: false
        }.first()
    }

    override fun observeBookmarks(): Flow<List<Bookmark>> {
        return dataStore.data.map { prefs ->
            prefs[bookmarksKey]?.let { json ->
                Json.decodeFromString<List<BookmarkData>>(json).map { data ->
                    Bookmark(
                        url = data.url,
                        title = data.title,
                        favicon = null,
                        addedAt = data.addedAt
                    )
                }
            } ?: emptyList()
        }
    }

    // ==================== HISTORY ====================

    private val historyKey = stringPreferencesKey("history")

    override suspend fun addHistory(url: String, title: String, timestamp: Long) {
        dataStore.edit { prefs ->
            val currentHistory = prefs[historyKey]?.let { json ->
                Json.decodeFromString<List<HistoryItemData>>(json)
            } ?: emptyList()

            val newItem = HistoryItemData(url, title, timestamp)

            // Keep last 100 items
            val updatedHistory = (listOf(newItem) + currentHistory)
                .distinctBy { it.url }
                .take(100)

            prefs[historyKey] = Json.encodeToString(updatedHistory)
        }
    }

    override fun observeHistory(): Flow<List<HistoryItem>> {
        return dataStore.data.map { prefs ->
            prefs[historyKey]?.let { json ->
                Json.decodeFromString<List<HistoryItemData>>(json).map { data ->
                    HistoryItem(data.url, data.title, data.timestamp)
                }
            } ?: emptyList()
        }
    }

    override suspend fun clearHistory() {
        dataStore.edit { it.remove(historyKey) }
    }

    // ==================== HELPERS ====================

    private fun sanitizeUrl(url: String): String {
        return url.replace(Regex("[^a-zA-Z0-9]"), "_")
    }
}

// ==================== SERIALIZABLE DATA CLASSES ====================
// Note: These must be kept in the same file or imported correctly.

@Serializable
private data class ConnectedWalletData(
    val walletId: String,
    val publicKey: String,
    val connectedAt: Long
)

@Serializable
private data class BookmarkData(
    val url: String,
    val title: String,
    val addedAt: Long
)

@Serializable
private data class HistoryItemData(
    val url: String,
    val title: String,
    val timestamp: Long
)