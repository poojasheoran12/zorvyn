package com.example.zorvyn.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.zorvyn.domain.model.BalanceState
import com.example.zorvyn.domain.model.Transaction
import com.example.zorvyn.domain.model.TransactionType
import com.example.zorvyn.domain.model.TransactionCategory

interface FinancialRepository {
    fun getBalanceState(): Flow<BalanceState>
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
    fun getTransactions(
        type: TransactionType? = null,
        category: TransactionCategory? = null,
        limit: Int? = null
    ): Flow<List<Transaction>>
    fun getCategorySummary(): Flow<Map<String, Double>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    fun exportToCsv(): Flow<String>
}
