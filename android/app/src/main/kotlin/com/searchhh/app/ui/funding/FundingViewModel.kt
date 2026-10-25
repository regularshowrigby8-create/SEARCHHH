package com.searchhh.app.ui.funding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.searchhh.app.core.ApiResult
import com.searchhh.app.core.AppError
import com.searchhh.app.core.DispatcherProvider
import com.searchhh.app.data.model.FundingMatch
import com.searchhh.app.data.model.FundingStage
import com.searchhh.app.data.repository.SearchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FundingUiState(
    val age: Int = 24,
    val stage: FundingStage = FundingStage.IDEA,
    val registered: Boolean = false,
    val trainingCompleted: Boolean = false,
    val annualTurnover: Double = 0.0,
    val eligibleCount: Int = 0,
    val matches: List<FundingMatch> = emptyList(),
    val nextActions: List<String> = emptyList(),
    val rulesReviewed: String = "",
    val isLoading: Boolean = false,
    val error: AppError? = null,
) {
    val eligible: List<FundingMatch> get() = matches.filter { it.isEligible }
    val needsSteps: List<FundingMatch> get() = matches.filter { it.needsSteps }
    val rulesReviewedLabel: String get() = if (rulesReviewed.isBlank()) "" else "Rules reviewed $rulesReviewed"
}

/**
 * "What can I get, today, with what I have?"
 *
 * The answer changes as the four inputs change, so every edit re-runs the
 * matcher (debounced by the UI being cheap: one small POST, no polling).
 */
class FundingViewModel(
    private val searchRepository: SearchRepository,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(FundingUiState())
    val state: StateFlow<FundingUiState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun setAge(age: Int) {
        _state.update { it.copy(age = age.coerceIn(15, 65)) }
        refresh()
    }

    fun setStage(stage: FundingStage) {
        _state.update { it.copy(stage = stage) }
        refresh()
    }

    fun setRegistered(registered: Boolean) {
        _state.update { it.copy(registered = registered) }
        refresh()
    }

    fun setTrainingCompleted(completed: Boolean) {
        _state.update { it.copy(trainingCompleted = completed) }
        refresh()
    }

    fun refresh() {
        val snapshot = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = withContext(dispatchers.io) {
                searchRepository.fundingMatch(
                    age = snapshot.age,
                    stage = snapshot.stage.apiValue,
                    registered = snapshot.registered,
                    trainingCompleted = snapshot.trainingCompleted,
                    annualTurnover = snapshot.annualTurnover,
                )
            }
            when (result) {
                is ApiResult.Success -> _state.update {
                    it.copy(
                        eligibleCount = result.data.eligibleCount,
                        matches = result.data.matches,
                        nextActions = result.data.nextActions,
                        rulesReviewed = result.data.rulesReviewed,
                        isLoading = false,
                        error = null,
                    )
                }
                is ApiResult.Failure -> _state.update { it.copy(isLoading = false, error = result.error) }
                ApiResult.Loading -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        fun factory(searchRepository: SearchRepository, dispatchers: DispatcherProvider) =
            viewModelFactory {
                initializer { FundingViewModel(searchRepository, dispatchers) }
            }
    }
}
