package com.searchhh.app.ui.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.searchhh.app.core.ApiResult
import com.searchhh.app.core.AppError
import com.searchhh.app.core.DispatcherProvider
import com.searchhh.app.core.PAGE_SIZE
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.model.SortOption
import com.searchhh.app.data.repository.SavedRepository
import com.searchhh.app.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ResultsUiState(
    val query: String = "",
    val category: String? = null,
    val sort: SortOption = SortOption.RELEVANCE,
    val results: List<Entry> = emptyList(),
    val savedIds: Set<String> = emptySet(),
    val total: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: AppError? = null,
    val fromCache: Boolean = false,
    val tookMs: Double = 0.0,
    val hasMore: Boolean = false,
) {
    val isEmpty: Boolean get() = results.isEmpty() && !isLoading && error == null
}

/**
 * Paginated results screen. Offset pagination (not Paging3) keeps the state
 * machine explicit and is plenty for 30-1000 documents.
 */
class ResultsViewModel(
    private val searchRepository: SearchRepository,
    private val savedRepository: SavedRepository,
    private val dispatchers: DispatcherProvider,
    initialQuery: String,
    initialCategory: String?,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ResultsUiState(query = initialQuery, category = initialCategory?.takeIf { it.isNotBlank() }),
    )
    val state: StateFlow<ResultsUiState> = _state.asStateFlow()

    private var offset = 0

    init {
        observeSaved()
        load(reset = true)
    }

    private fun observeSaved() {
        viewModelScope.launch {
            savedRepository.saved.collect { entries ->
                _state.update { it.copy(savedIds = entries.map(Entry::id).toSet()) }
            }
        }
    }

    fun retry() = load(reset = true)

    fun changeSort(sort: SortOption) {
        if (sort == _state.value.sort) return
        _state.update { it.copy(sort = sort) }
        load(reset = true)
    }

    fun setCategory(category: String?) {
        _state.update { it.copy(category = category) }
        load(reset = true)
    }

    fun loadMore() {
        val current = _state.value
        if (current.isLoading || current.isLoadingMore || !current.hasMore) return
        _state.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val page = withContext(dispatchers.io) {
                searchRepository.search(
                    query = current.query,
                    limit = PAGE_SIZE,
                    offset = offset,
                    category = current.category,
                    sort = current.sort.apiValue,
                )
            }
            when (page) {
                is ApiResult.Success -> {
                    offset += page.data.results.size
                    _state.update {
                        it.copy(
                            results = it.results + page.data.results,
                            total = page.data.total,
                            isLoadingMore = false,
                            hasMore = offset < page.data.total && page.data.results.isNotEmpty(),
                        )
                    }
                }
                is ApiResult.Failure -> _state.update { it.copy(isLoadingMore = false, error = page.error) }
                ApiResult.Loading -> _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    fun toggleSave(entry: Entry) {
        viewModelScope.launch { savedRepository.toggle(entry) }
    }

    private fun load(reset: Boolean) {
        val current = _state.value
        if (reset) offset = 0
        _state.update { it.copy(isLoading = true, error = null, results = if (reset) emptyList() else it.results) }
        viewModelScope.launch {
            val page = withContext(dispatchers.io) {
                searchRepository.search(
                    query = current.query,
                    limit = PAGE_SIZE,
                    offset = if (reset) 0 else offset,
                    category = current.category,
                    sort = current.sort.apiValue,
                )
            }
            when (page) {
                is ApiResult.Success -> {
                    offset = page.data.results.size
                    _state.update {
                        it.copy(
                            results = page.data.results,
                            total = page.data.total,
                            isLoading = false,
                            fromCache = page.data.fromCache,
                            tookMs = page.data.tookMs,
                            hasMore = offset < page.data.total && page.data.results.isNotEmpty(),
                            error = null,
                        )
                    }
                }
                is ApiResult.Failure -> _state.update { it.copy(isLoading = false, error = page.error) }
                ApiResult.Loading -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(
            searchRepository: SearchRepository,
            savedRepository: SavedRepository,
            dispatchers: DispatcherProvider,
            query: String,
            category: String?,
        ) = viewModelFactory {
            initializer {
                ResultsViewModel(searchRepository, savedRepository, dispatchers, query, category)
            }
        }
    }
}
