package com.example.zorvyn.domain.repository

import kotlinx.coroutines.flow.Flow
import com.example.zorvyn.domain.model.Budget

interface BudgetRepository {
    fun getBudgets(): Flow<List<Budget>>
    suspend fun addBudget(budget: Budget)
    suspend fun deleteBudget(id: String)
    suspend fun seedBudgetData()
}
