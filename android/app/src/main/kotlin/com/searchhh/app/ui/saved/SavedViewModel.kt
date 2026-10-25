package com.searchhh.app.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.searchhh.app.data.model.Entry
import com.searchhh.app.data.repository.SavedRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavedUiState(
    val entries: List<Entry> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * Saved tab. Backed entirely by Room, so it renders with the device offline -
 * the single most useful demo behaviour for a hackathon.
 */
class SavedViewModel(private val savedRepository: SavedRepository) : ViewModel() {

    val state: StateFlow<SavedUiState> = savedRepository.saved
        .map { entries -> SavedUiState(entries = entries, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SavedUiState(),
        )

    fun remove(entryId: String) {
        viewModelScope.launch { savedRepository.remove(entryId) }
    }

    fun clearAll() {
        viewModelScope.launch { savedRepository.clear() }
    }

    companion object {
        fun factory(savedRepository: SavedRepository) = viewModelFactory {
            initializer { SavedViewModel(savedRepository) }
        }
    }
}
