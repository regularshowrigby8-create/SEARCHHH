package com.searchhh.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.searchhh.app.core.ApiResult
import com.searchhh.app.core.AppError
import com.searchhh.app.core.DispatcherProvider
import com.searchhh.app.data.model.CategoryCount
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.model.HealthInfo
import com.searchhh.app.data.repository.RecentQueriesRepository
import com.searchhh.app.data.repository.SearchRepository
import com.searchhh.app.data.repository.SavedRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

data class HomeUiState(
    val query: String = "",
    val suggestions: List<String> = emptyList(),
    val recent: List<String> = emptyList(),
    val categories: List<CategoryCount> = emptyList(),
    val trending: List<Entry> = emptyList(),
    val savedIds: Set<String> = emptySet(),
    val backend: HealthInfo? = null,
    val isLoading: Boolean = false,
    val error: AppError? = null,
    val showingCache: Boolean = false,
    val tookMs: Double = 0.0,
)

/**
 * Home screen logic: discovery feed, category facets, debounced suggestions.
 * All state is a single immutable [HomeUiState] rendered by a stateless screen.
 */
class HomeViewModel(
    private val searchRepository: SearchRepository,
    private val savedRepository: SavedRepository,
    private val recentQueriesRepository: RecentQueriesRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private var suggestJob: Job? = null

    init {
        observeSaved()
        observeRecent()
        loadDiscovery()
    }

    private fun observeSaved() {
        viewModelScope.launch {
            savedRepository.saved.collect { entries ->
                _state.update { it.copy(savedIds = entries.map(Entry::id).toSet()) }
            }
        }
    }

    private fun observeRecent() {
        viewModelScope.launch {
            recentQueriesRepository.recent.collect { queries ->
                _state.update { it.copy(recent = queries.take(6)) }
            }
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
        suggestJob?.cancel()
        if (value.length < 2) {
            _state.update { it.copy(suggestions = emptyList()) }
            return
        }
        suggestJob = viewModelScope.launch {
            delay(250) // debounce: one request per pause, not per keystroke
            val suggestions = withContext(dispatchers.io) { searchRepository.suggest(value) }
            _state.update { it.copy(suggestions = suggestions) }
        }
    }

    fun onQuerySubmitted(query: String = _state.value.query) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { recentQueriesRepository.record(trimmed) }
        _state.update { it.copy(suggestions = emptyList()) }
    }

    fun loadDiscovery() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val trending = withContext(dispatchers.io) {
                searchRepository.search(query = "", limit = 6, sort = "rating")
            }
            val categories = withContext(dispatchers.io) { searchRepository.categories() }
            val backend = withContext(dispatchers.io) { searchRepository.health() }

            when (trending) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        trending = trending.data.results,
                        isLoading = false,
                        showingCache = trending.data.fromCache,
                        tookMs = trending.data.tookMs,
                        error = null,
                    )
                }
                is ApiResult.Failure -> _state.update { it.copy(isLoading = false, error = trending.error) }
                ApiResult.Loading -> Unit
            }

            if (categories is ApiResult.Success) {
                _state.update { it.copy(categories = categories.data) }
            }
            if (backend is ApiResult.Success) {
                _state.update { it.copy(backend = backend.data) }
            } else if (backend is ApiResult.Failure) {
                Timber.w("Health check failed: %s", backend.error)
            }
        }
    }

    fun toggleSave(entry: Entry) {
        viewModelScope.launch { savedRepository.toggle(entry) }
    }

    companion object {
        fun factory(
            searchRepository: SearchRepository,
            savedRepository: SavedRepository,
            recentQueriesRepository: RecentQueriesRepository,
            dispatchers: DispatcherProvider,
        ) = viewModelFactory {
            initializer {
                HomeViewModel(searchRepository, savedRepository, recentQueriesRepository, dispatchers)
            }
        }
    }
}
