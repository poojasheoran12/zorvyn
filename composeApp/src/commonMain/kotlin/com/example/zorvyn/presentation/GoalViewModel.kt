package com.example.zorvyn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zorvyn.domain.model.Goal
import com.example.zorvyn.domain.repository.FinancialRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class GoalUiState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = false
)

class GoalViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        _uiState.update { it.copy(isLoading = true) }
        repository.getGoals()
            .onEach { list ->
                _uiState.update { it.copy(goals = list, isLoading = false) }
            }.launchIn(viewModelScope)
    }

    fun addGoal(
        name: String,
        targetAmount: Double,
        savedAmount: Double,
        desiredDate: Instant,
        icon: String
    ) {
        viewModelScope.launch {
            val goal = Goal(
                id = Clock.System.now().toEpochMilliseconds().toString(),
                name = name,
                targetAmount = targetAmount,
                savedAmount = savedAmount,
                desiredDate = desiredDate,
                icon = icon,
                createdAt = Clock.System.now()
            )
            repository.addGoal(goal)
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }
}
