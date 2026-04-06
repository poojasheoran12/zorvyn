package com.example.zorvyn.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.zorvyn.domain.model.Transaction
import com.example.zorvyn.domain.model.TransactionType
import com.example.zorvyn.domain.model.TransactionCategory

interface TransactionRepository {
    fun getTransactions(): Flow<List<Transaction>>
    suspend fun addTransaction(transaction: Transaction)
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(id: String)
    suspend fun seedTransactionData()
}
