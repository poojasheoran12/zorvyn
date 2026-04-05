package org.example.project.domain.repository

import kotlinx.coroutines.flow.Flow
import org.example.project.domain.model.BalanceState
import org.example.project.domain.model.Transaction

interface FinancialRepository {
    fun getBalanceState(): Flow<BalanceState>
    fun getRecentTransactions(limit: Int): Flow<List<Transaction>>
    fun getCategorySummary(): Flow<Map<String, Double>>
}
