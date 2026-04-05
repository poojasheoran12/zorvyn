package org.example.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.example.project.domain.model.*
import org.example.project.domain.repository.FinancialRepository

class FinancialRepositoryImpl : FinancialRepository {
    override fun getBalanceState(): Flow<BalanceState> = flow {
        emit(
            BalanceState(
                currentBalance = 12450.0,
                income = 20000.0,
                expense = 7550.0,
                savings = 12450.0
            )
        )
    }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> = flow {
        emit(
            listOf(
                Transaction("1", 750.0, "Lunch", TransactionType.EXPENSE, TransactionCategory.FOOD),
                Transaction("2", 1200.0, "Fuel", TransactionType.EXPENSE, TransactionCategory.TRAVEL),
                Transaction("3", 2500.0, "Dress", TransactionType.EXPENSE, TransactionCategory.SHOPPING)
            ).take(limit)
        )
    }

    override fun getCategorySummary(): Flow<Map<String, Double>> = flow {
        emit(
            mapOf(
                "Food" to 750.0,
                "Travel" to 1200.0,
                "Shopping" to 2500.0,
                "Others" to 3100.0
            )
        )
    }
}
