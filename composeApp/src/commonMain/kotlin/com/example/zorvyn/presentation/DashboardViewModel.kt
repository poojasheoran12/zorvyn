package com.example.zorvyn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.domain.repository.FinancialRepository

data class DashboardUiState(
    val balance: BalanceState = BalanceState(0.0, 0.0, 0.0, 0.0),
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryChartData: Map<String, Double> = emptyMap(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val repository: FinancialRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        combine(
            repository.getBalanceState(),
            repository.getRecentTransactions(3),
            repository.getCategorySummary()
        ) { balance, transactions, categories ->
            DashboardUiState(
                balance = balance,
                recentTransactions = transactions,
                categoryChartData = categories,
                isLoading = false
            )
        }.onEach { state ->
            _uiState.update { state }
        }.launchIn(viewModelScope)
    }
}
