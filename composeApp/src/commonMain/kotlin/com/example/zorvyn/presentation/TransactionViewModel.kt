package com.example.zorvyn.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.zorvyn.domain.model.*
import com.example.zorvyn.domain.repository.TransactionRepository
import com.example.zorvyn.util.ReceiptData
import com.example.zorvyn.util.TextRecognizer
import com.example.zorvyn.util.FileExporter

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
    private val transactionRepository: TransactionRepository,
    private val textRecognizer: TextRecognizer,
    private val fileExporter: FileExporter
) : ViewModel() {

    fun downloadData() {
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getTransactions().first()
                val csvContent = formatTransactionsToCsv(transactions)
                fileExporter.saveAndShare("zorvyn_transactions.csv", csvContent)
            } catch (e: Exception) {
                // Error handling
            }
        }
    }

    private fun formatTransactionsToCsv(list: List<Transaction>): String {
        val header = "ID,Amount,Note,Type,Category,Date\n"
        val rows = list.joinToString("\n") { 
            "${it.id},${it.amount},\"${it.note}\",${it.type},${it.category},${it.formattedDate}"
        }
        return header + rows
    }

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        _uiState.update { it.copy(isLoading = true) }
        
        transactionRepository.getTransactions()
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
                TransactionFilter.THIS_MONTH -> true // Simplified for demo
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
            transactionRepository.addTransaction(newTransaction)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }
}
