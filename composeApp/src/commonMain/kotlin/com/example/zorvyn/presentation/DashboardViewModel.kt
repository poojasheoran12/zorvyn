package com.example.zorvyn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.domain.repository.TransactionRepository

data class DashboardUiState(
    val balance: BalanceState = BalanceState(0.0, 0.0, 0.0, 0.0),
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryChartData: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val transactions = transactionRepository.getTransactions().first()
            if (transactions.isEmpty()) {
                transactionRepository.seedTransactionData()
            }
        }
        loadDashboardData()
    }

    private fun loadDashboardData() {
        transactionRepository.getTransactions().onEach { list ->
            val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val savings = income - expense
            val goal = 5000.0
            val progress = if (goal > 0) (savings / goal) else 0.0

            val streak = list.groupBy { 
                it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).dayOfYear 
            }.size.coerceAtMost(7)

            val feedback = when {
                progress >= 1.0 -> "Goal Reached! Amazing work! 🎯"
                progress >= 0.6 -> "You are ${ (progress * 100).toInt() }% closer to your goal 🎯"
                list.any { it.category == TransactionCategory.FOOD && it.amount > 1000 } -> 
                    "Food spending increased this week ⚠️"
                savings > 0 -> "You spent less than yesterday 👏"
                else -> "Keep it up! Small steps lead to big reach."
            }

            val balanceState = BalanceState(savings, income, expense, savings, goal, streak, feedback)
            val categorySummary = list.filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category.name }
                .mapValues { it.value.sumOf { tx -> tx.amount } }

            _uiState.update { 
                it.copy(
                    balance = balanceState,
                    recentTransactions = list.take(3),
                    categoryChartData = categorySummary,
                    isLoading = false
                )
            }
        }.launchIn(viewModelScope)
    }
}
