package com.searchhh.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.searchhh.app.core.ApiResult
import com.searchhh.app.core.AppError
import com.searchhh.app.core.DispatcherProvider
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.model.Insight
import com.searchhh.app.data.repository.SavedRepository
import com.searchhh.app.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DetailUiState(
    val entry: Entry? = null,
    val insight: Insight? = null,
    val isInsightLoading: Boolean = false,
    val insightError: AppError? = null,
    val isSaved: Boolean = false,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val isSendingFeedback: Boolean = false,
    val message: String? = null,
)

class DetailViewModel(
    private val searchRepository: SearchRepository,
    private val savedRepository: SavedRepository,
    private val dispatchers: DispatcherProvider,
    private val entryId: String,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        observeSaved()
        load()
    }

    private fun observeSaved() {
        viewModelScope.launch {
            savedRepository.isSaved(entryId).collect { saved ->
                _state.update { it.copy(isSaved = saved) }
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = withContext(dispatchers.io) { searchRepository.entry(entryId) }
            when (result) {
                is ApiResult.Success -> {
                    _state.update { it.copy(entry = result.data, isLoading = false, error = null) }
                    loadInsight()
                }
                is ApiResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun loadInsight() {
        if (_state.value.isInsightLoading) return
        viewModelScope.launch {
            _state.update { it.copy(isInsightLoading = true, insightError = null) }
            val result = withContext(dispatchers.io) { searchRepository.insight(entryId) }
            when (result) {
                is ApiResult.Success -> _state.update {
                    it.copy(insight = result.data, isInsightLoading = false)
                }
                is ApiResult.Failure -> _state.update {
                    it.copy(isInsightLoading = false, insightError = result.error)
                }
                ApiResult.Loading -> _state.update { it.copy(isInsightLoading = false) }
            }
        }
    }

    fun toggleSave() {
        val entry = _state.value.entry ?: return
        viewModelScope.launch {
            val nowSaved = savedRepository.toggle(entry)
            _state.update { it.copy(message = if (nowSaved) "Saved for offline" else "Removed from saved") }
        }
    }

    fun sendFeedback(message: String, rating: Int) {
        if (message.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSendingFeedback = true) }
            val result = withContext(dispatchers.io) {
                searchRepository.feedback(message = message, rating = rating, entryId = entryId)
            }
            _state.update {
                it.copy(
                    isSendingFeedback = false,
                    message = when (result) {
                        is ApiResult.Success -> "Thanks! Feedback received."
                        is ApiResult.Failure -> "Couldn't send feedback (${result.error})"
                        ApiResult.Loading -> null
                    },
                )
            }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    companion object {
        fun factory(
            searchRepository: SearchRepository,
            savedRepository: SavedRepository,
            dispatchers: DispatcherProvider,
            entryId: String,
        ) = viewModelFactory {
            initializer { DetailViewModel(searchRepository, savedRepository, dispatchers, entryId) }
        }
    }
}
