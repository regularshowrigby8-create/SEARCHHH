package com.searchhh.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.searchhh.app.BuildConfig
import com.searchhh.app.core.ApiResult
import com.searchhh.app.core.DispatcherProvider
import com.searchhh.app.data.local.AppSettings
import com.searchhh.app.data.local.SettingsRepository
import com.searchhh.app.data.model.HealthInfo
import com.searchhh.app.data.repository.RecentQueriesRepository
import com.searchhh.app.data.repository.SavedRepository
import com.searchhh.app.data.repository.SearchRepository
import com.searchhh.app.telemetry.Telemetry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val backend: HealthInfo? = null,
    val isCheckingBackend: Boolean = false,
    val backendError: String? = null,
    val savedCount: Int = 0,
    val pushToken: String? = null,
    val message: String? = null,
    val appVersion: String = BuildConfig.VERSION_NAME,
    val gitSha: String = BuildConfig.GIT_SHA,
    val effectiveBaseUrl: String = BuildConfig.API_BASE_URL,
)

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val searchRepository: SearchRepository,
    private val savedRepository: SavedRepository,
    private val recentQueriesRepository: RecentQueriesRepository,
    private val dispatchers: DispatcherProvider,
    private val updateBaseUrl: (String) -> Unit,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        observeSettings()
        refreshBackend()
        refreshCounts()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { it.copy(settings = settings, effectiveBaseUrl = settings.apiBaseUrl) }
            }
        }
    }

    private fun refreshCounts() {
        viewModelScope.launch {
            val count = savedRepository.count()
            _state.update { it.copy(savedCount = count) }
        }
    }

    fun refreshBackend() {
        viewModelScope.launch {
            _state.update { it.copy(isCheckingBackend = true, backendError = null) }
            when (val result = withContext(dispatchers.io) { searchRepository.health() }) {
                is ApiResult.Success -> _state.update {
                    it.copy(backend = result.data, isCheckingBackend = false, backendError = null)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(isCheckingBackend = false, backendError = result.error.name)
                }
                ApiResult.Loading -> _state.update { it.copy(isCheckingBackend = false) }
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDarkMode(enabled) }
    }

    fun setTelemetry(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setTelemetryEnabled(enabled) }
    }

    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNotificationsEnabled(enabled) }
    }

    fun setApiBaseUrl(url: String) {
        val trimmed = url.trim()
        if (!trimmed.startsWith("http")) {
            _state.update { it.copy(message = "URL must start with http:// or https://") }
            return
        }
        viewModelScope.launch {
            settingsRepository.setApiBaseUrl(trimmed)
            updateBaseUrl(trimmed)
            searchRepository.invalidateCache()
            _state.update { it.copy(message = "API base URL updated") }
            refreshBackend()
        }
    }

    fun clearCaches() {
        viewModelScope.launch {
            searchRepository.invalidateCache()
            recentQueriesRepository.clear()
            _state.update { it.copy(message = "Cached catalog cleared") }
        }
    }

    fun clearSaved() {
        viewModelScope.launch {
            savedRepository.clear()
            refreshCounts()
            _state.update { it.copy(message = "Saved items removed") }
        }
    }

    fun sendTestCrash() {
        Telemetry.sendTestError("Manual test from Settings")
        _state.update { it.copy(message = "Test error reported to Sentry/Crashlytics") }
    }

    fun loadPushToken() {
        Telemetry.currentPushToken { token ->
            _state.update { it.copy(pushToken = token ?: "not configured") }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    companion object {
        fun factory(
            settingsRepository: SettingsRepository,
            searchRepository: SearchRepository,
            savedRepository: SavedRepository,
            recentQueriesRepository: RecentQueriesRepository,
            dispatchers: DispatcherProvider,
            updateBaseUrl: (String) -> Unit,
        ) = viewModelFactory {
            initializer {
                SettingsViewModel(
                    settingsRepository,
                    searchRepository,
                    savedRepository,
                    recentQueriesRepository,
                    dispatchers,
                    updateBaseUrl,
                )
            }
        }
    }
}
