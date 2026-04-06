package com.example.zorvyn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.domain.repository.BudgetRepository
import com.example.zorvyn.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*

data class BudgetUiState(
    val budgets: List<Budget> = emptyList(),
    val streak: Int = 0,
    val isLoading: Boolean = false
)

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val budgets = budgetRepository.getBudgets().first()
            if (budgets.isEmpty()) {
                budgetRepository.seedBudgetData()
            }
        }
        loadData()
    }

    private fun loadData() {
        _uiState.update { it.copy(isLoading = true) }

        combine(
            budgetRepository.getBudgets(),
            transactionRepository.getTransactions()
        ) { budgets, allTransactions ->
            val transactions = allTransactions.filter { it.type == TransactionType.EXPENSE }
            val activeBudget = budgets.firstOrNull { it.isActive }
            val streak = if (activeBudget != null) {
                calculateStreak(activeBudget, transactions)
            } else 0

            BudgetUiState(budgets = budgets, streak = streak, isLoading = false)
        }.onEach { state ->
            _uiState.value = state
        }.launchIn(viewModelScope)
    }

    private fun calculateStreak(budget: Budget, transactions: List<Transaction>): Int {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        var currentStreak = 0

        for (i in 0 until 30) { 
            val date = today.minus(i, DateTimeUnit.DAY)
            if (date < budget.startDate.toLocalDateTime(TimeZone.currentSystemDefault()).date) break

            val dayExpenses = transactions.filter {
                it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).date == date
            }.sumOf { it.amount }

            if (dayExpenses <= budget.dailyLimit) {
                currentStreak++
            } else if (i == 0) {

                continue 
            } else {
                break 
            }
        }
        return currentStreak
    }

    fun addBudget(name: String, total: Double, daily: Double, days: Int) {
        viewModelScope.launch {
            val budget = Budget(
                id = Clock.System.now().toEpochMilliseconds().toString(),
                name = name,
                totalBudget = total,
                dailyLimit = daily,
                durationDays = days,
                startDate = Clock.System.now(),
                createdAt = Clock.System.now()
            )
            budgetRepository.addBudget(budget)
        }
    }

    fun deleteBudget(id: String) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(id)
        }
    }
}
