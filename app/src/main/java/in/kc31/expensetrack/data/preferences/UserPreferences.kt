package `in`.kc31.expensetrack.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        
        private val SERVER_URL = stringPreferencesKey("server_url")
        private val EMAIL = stringPreferencesKey("email")
        private val PASSWORD = stringPreferencesKey("password")
        private val SENDER_LIST = stringPreferencesKey("sender_list")
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val LAST_FETCH_TIMESTAMP = longPreferencesKey("last_fetch_timestamp")
        private val SENT_SMS_IDS = stringPreferencesKey("sent_sms_ids")
    }

    // Server URL
    val serverUrl: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SERVER_URL] ?: ""
    }

    suspend fun saveServerUrl(url: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_URL] = url
        }
    }

    // Email
    val email: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[EMAIL] ?: ""
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { preferences ->
            preferences[EMAIL] = email
        }
    }

    // Password
    val password: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PASSWORD] ?: ""
    }

    suspend fun savePassword(password: String) {
        context.dataStore.edit { preferences ->
            preferences[PASSWORD] = password
        }
    }

    // Sender List
    val senderList: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SENDER_LIST] ?: ""
    }

    suspend fun saveSenderList(senderList: String) {
        context.dataStore.edit { preferences ->
            preferences[SENDER_LIST] = senderList
        }
    }

    // Access Token
    val accessToken: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN] ?: ""
    }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN] = token
        }
    }

    // Last Fetch Timestamp
    val lastFetchTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_FETCH_TIMESTAMP] ?: 0L
    }

    suspend fun saveLastFetchTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_FETCH_TIMESTAMP] = timestamp
        }
    }

    // Sent SMS IDs
    val sentSmsIds: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        val idsString = preferences[SENT_SMS_IDS] ?: ""
        if (idsString.isEmpty()) emptySet() else idsString.split(",").toSet()
    }

    suspend fun saveSentSmsId(id: String) {
        context.dataStore.edit { preferences ->
            val currentIds = preferences[SENT_SMS_IDS] ?: ""
            val idSet = if (currentIds.isEmpty()) {
                setOf(id)
            } else {
                currentIds.split(",").toMutableSet().apply { add(id) }
            }
            preferences[SENT_SMS_IDS] = idSet.joinToString(",")
        }
    }
}
