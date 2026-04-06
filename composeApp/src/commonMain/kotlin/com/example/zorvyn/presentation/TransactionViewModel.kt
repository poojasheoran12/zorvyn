package com.example.zorvyn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.domain.repository.FinancialRepository
import com.example.zorvyn.util.ReceiptData
import com.example.zorvyn.util.TextRecognizer

enum class TransactionFilter {
    ALL, INCOME, EXPENSE, THIS_MONTH
}

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val currentFilter: TransactionFilter = TransactionFilter.ALL,
    val searchQuery: String = "",
    val isLoading: Boolean = false
)

class TransactionViewModel(
    private val repository: FinancialRepository,
    private val textRecognizer: TextRecognizer
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        _uiState.update { it.copy(isLoading = true) }
        
        repository.getTransactions()
            .onEach { list ->
                _uiState.update { it.copy(transactions = list, isLoading = false) }
            }.launchIn(viewModelScope)
    }

    fun setFilter(filter: TransactionFilter) {
        _uiState.update { it.copy(currentFilter = filter) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        _uiState,
        _uiState.map { it.transactions }
    ) { state, transactions ->
        transactions.filter {
            val matchesFilter = when (state.currentFilter) {
                TransactionFilter.ALL -> true
                TransactionFilter.INCOME -> it.type == TransactionType.INCOME
                TransactionFilter.EXPENSE -> it.type == TransactionType.EXPENSE
                TransactionFilter.THIS_MONTH -> true // Simplification
            }
            val matchesSearch = it.note.contains(state.searchQuery, ignoreCase = true) ||
                    it.category.name.contains(state.searchQuery, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun processReceipt(imageBytes: ByteArray): ReceiptData {
        return textRecognizer.recognizeText(imageBytes)
    }

    fun addTransaction(
        amount: Double,
        note: String,
        type: TransactionType,
        category: TransactionCategory,
        receiptPath: String?
    ) {
        viewModelScope.launch {
            val newTransaction = Transaction(
                id = (System.currentTimeMillis()).toString(),
                amount = amount,
                note = note,
                type = type,
                category = category,
                receiptPath = receiptPath
            )
            repository.addTransaction(newTransaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun exportToCsv(): Flow<String> = repository.exportToCsv()
}
