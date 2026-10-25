package com.searchhh.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.searchhh.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

/** DataStore instance - one per process, created lazily by the delegate. */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "searchhh_settings")

data class AppSettings(
    /** null = follow the system setting. */
    val darkMode: Boolean? = null,
    val telemetryEnabled: Boolean = true,
    val apiBaseUrl: String = BuildConfig.API_BASE_URL,
    val lastSyncAt: Long = 0L,
    val notificationsEnabled: Boolean = true,
) {
    /** True when the user pointed the app at a non-default backend. */
    val isCustomBackend: Boolean get() = apiBaseUrl != BuildConfig.API_BASE_URL
}

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val TELEMETRY = booleanPreferencesKey("telemetry_enabled")
        val API_BASE_URL = stringPreferencesKey("api_base_url")
        val LAST_SYNC = stringPreferencesKey("last_sync")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
    }

    val settings: Flow<AppSettings> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                Timber.w(throwable, "Falling back to default settings")
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            AppSettings(
                darkMode = preferences[Keys.DARK_MODE],
                telemetryEnabled = preferences[Keys.TELEMETRY] ?: true,
                apiBaseUrl = preferences[Keys.API_BASE_URL] ?: BuildConfig.API_BASE_URL,
                lastSyncAt = preferences[Keys.LAST_SYNC]?.toLongOrNull() ?: 0L,
                notificationsEnabled = preferences[Keys.NOTIFICATIONS] ?: true,
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setTelemetryEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.TELEMETRY] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun setApiBaseUrl(url: String) {
        val normalised = if (url.endsWith("/")) url else "$url/"
        dataStore.edit { it[Keys.API_BASE_URL] = normalised }
    }

    suspend fun markSynced(timestamp: Long = System.currentTimeMillis()) {
        dataStore.edit { it[Keys.LAST_SYNC] = timestamp.toString() }
    }
}
