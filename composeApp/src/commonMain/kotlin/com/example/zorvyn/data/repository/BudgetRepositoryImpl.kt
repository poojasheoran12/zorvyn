package com.example.zorvyn.data.repository

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import com.example.zorvyn.domain.model.Budget
import com.example.zorvyn.domain.repository.BudgetRepository
import com.example.zorvyn.database.AppDatabase
import com.example.zorvyn.database.BudgetEntity
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList

class BudgetRepositoryImpl(
    private val firestore: FirebaseFirestore,
    database: AppDatabase
) : BudgetRepository {
    private val collection = firestore.collection("budgets")
    private val queries = database.appDatabaseQueries
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        scope.launch {
            try {
                collection.snapshots.collect { snapshot ->
                    val remote = snapshot.documents.map { it.data<Budget>() }
                    remote.forEach { 
                        queries.insertBudget(
                            it.id, it.name, it.totalBudget, it.dailyLimit, 
                            it.durationDays.toLong(), it.startDate.toEpochMilliseconds(), it.createdAt.toEpochMilliseconds()
                        )
                    }
                }
            } catch (e: Exception) {}
        }
    }

    override fun getBudgets(): Flow<List<Budget>> = 
        queries.getBudgets().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun addBudget(budget: Budget) {
        queries.insertBudget(
            budget.id, budget.name, budget.totalBudget, budget.dailyLimit, 
            budget.durationDays.toLong(), budget.startDate.toEpochMilliseconds(), budget.createdAt.toEpochMilliseconds()
        )
        try {
            collection.document(budget.id).set(budget)
        } catch (e: Exception) {}
    }

    override suspend fun deleteBudget(id: String) {
        queries.deleteBudget(id)
        try {
            collection.document(id).delete()
        } catch (e: Exception) {}
    }

    override suspend fun seedBudgetData() {
        val now = Clock.System.now()
        val mock = listOf(
            Budget("b1", "Weekly Groceries", 5000.0, 700.0, 7, now, now),
            Budget("b2", "Commute Budget", 2000.0, 300.0, 30, now, now)
        )
        mock.forEach { addBudget(it) }
    }

    private fun BudgetEntity.toDomain() = Budget(
        id = id, name = name, totalBudget = totalBudget, 
        dailyLimit = dailyLimit, durationDays = durationDays.toInt(),
        startDate = Instant.fromEpochMilliseconds(startDate), createdAt = Instant.fromEpochMilliseconds(createdAt)
    )
}
